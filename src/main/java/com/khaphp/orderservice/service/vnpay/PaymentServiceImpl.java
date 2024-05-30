package com.khaphp.orderservice.service.vnpay;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.dto.payment.WalletDTOupdate;
import com.khaphp.common.dto.payment.WalletTransactionDTOcreate;
import com.khaphp.common.entity.UserSystem;
import com.khaphp.orderservice.call.payment.PaymentServiceCall;
import com.khaphp.orderservice.call.userservice.UserServiceCall;
import com.khaphp.orderservice.constant.StatusOrder;
import com.khaphp.orderservice.constant.StatusResponse;
import com.khaphp.orderservice.constant.ThirdParty;
import com.khaphp.orderservice.entity.Order;
import com.khaphp.orderservice.entity.OrderDetail;
import com.khaphp.orderservice.repo.OrdersRepository;
import com.khaphp.orderservice.service.kafka.KafkaMessagePulisher;
import com.khaphp.orderservice.util.VnPayHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final VnPayHelper vnPayHelper;
    private final UserServiceCall userServiceCall;
    private final PaymentServiceCall paymentServiceCall;
    private final OrdersRepository ordersRepository;
    private final KafkaMessagePulisher kafkaMessagePulisher;

    @Value("${aws.s3.link_bucket}")
    private String linkBucket;
    @Override
    public ResponseObject<Object> createPayment(HttpServletRequest req, int amountParam, String customerId, boolean isThirdParty, String orderId) {
        try {
            //check id customer
            UserSystem userSystem = userServiceCall.getObject(customerId);
//            if (userSystem == null || !userSystem.getRole().equals(Role.CUSTOMER)) {
//                throw new ObjectNotFound("User not found");
//            }
            userSystem.setImgUrl(userSystem.getImgUrl().substring(linkBucket.length()));

            //create param for vnpay-url required
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "other";
            long amount = amountParam * 100;       //số tiền phải nhận với 100, quy định của vn pay
//        String bankCode = req.getParameter("bankCode");
            //nếu ko truyền giá trị này thì nó sẽ cho ta chọn ngân hàng để thanh toán
            String vnp_TxnRef = vnPayHelper.getRandomNumber(8);
            String vnp_IpAddr = vnPayHelper.getIpAddress(req);    //kham khảo vnp_IpAddr tại https://sandbox.vnpayment.vn/apis/files/VNPAY%20Payment%20Gateway_Techspec%202.1.0-VN.pdf

            String vnp_TmnCode = vnPayHelper.vnp_TmnCode;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");      //hiện gi chỉ hỗ trợ VND

//        if (bankCode != null && !bankCode.isEmpty()) {    //vì ta ko cần bankcode vì ta sẽ tự chọn ngân hàng test, nhìn nó vip bro hơn :))
//            vnp_Params.put("vnp_BankCode", bankCode);     //còn nếu mú chỉ định thì cứ ghi ngân hàng đó ra, chữ in hoa, vd: NCB, VIETCOMBANK, ..
//        }
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            if(isThirdParty){
                vnp_Params.put("vnp_OrderInfo", ThirdParty.THIRD_PARTY_MSG +orderId+": " + vnp_TxnRef + " cua UserID: " + customerId);
            }else{
                vnp_Params.put("vnp_OrderInfo", "Nap tien vao vi Energy Handbook: " + vnp_TxnRef + " cua UserID: " + customerId);   //chữ ko dấu nha
            }
            vnp_Params.put("vnp_OrderType", orderType);

//        String locate = req.getParameter("language");
            vnp_Params.put("vnp_Locale", "vn"); //fix cứng luôn vì vnpay hiện tại nó chỉ hỗ trợ tại vietnam
            vnp_Params.put("vnp_ReturnUrl", vnPayHelper.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance();   //() -> TimeZone.getTimeZone("UTC") //Etc/GMT+7
//            cld.add(Calendar.HOUR, 7); //vì trên server nó ko nhận giờ GMT+7 nên phải add tay khúc này
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            log.info("==> vnp_CreateDate: " + vnp_CreateDate);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List fieldNames = new ArrayList(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = vnPayHelper.hmacSHA512(vnPayHelper.secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnPayHelper.vnp_PayUrl + "?" + queryUrl;

            return ResponseObject.builder()
                    .code(200).message("create url payment success")
                    .data(paymentUrl)
                    .build();

            //trả về kiểu này để nó tự chuyển hướng qua url payment của VNpay, kết thức api hiện tại
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.LOCATION, paymentUrl);
//            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            return ResponseObject.builder()
                    .code(400).message("Exception: " + e.getMessage())
                    .build();
        }
    }

    public static void main(String[] args) {
        String info = "3party 9483or39r3r4fdfdf2wr4c2: 32987430 cua UserID: 9437h-r49i7hf4-fkuhf387fdwu";
        log.info(info.substring(ThirdParty.THIRD_PARTY_MSG.length(), info.indexOf(':')));
    }
    /*
    * http://localhost:8080/api/payment/payment_result
    ?vnp_Amount=1000000
    &vnp_BankCode=NCB
    &vnp_BankTranNo=VNP14370274
    &vnp_CardType=ATM
    &vnp_OrderInfo=Thanh+toan+don+hang%3A+91730521+cua+UserID:3A+867071a2-b0c8-48e3-9cf3-19815f8fb45e
    &vnp_PayDate=20240408114800
    &vnp_ResponseCode=00
    &vnp_TmnCode=QE1PYQ1B
    &vnp_TransactionNo=14370274
    &vnp_TransactionStatus=00
    &vnp_TxnRef=91730521
    &vnp_SecureHash=91d92dc780297f47f071480913a7a59bb932cf6bb309480d782e0e869d70d05da2d8e3cc98473a416270a4001a76859fd2f4835257233df11e9bda885caf1afd
        * */

    @Override
    public ResponseObject<Object> resultTransaction(String vnpAmount, String vnpBankCode, String vnpOrderInfo, String vnpPayDate, String vnpResponseCode) {
        try {
            ResponseObject<Object> responseObject = new ResponseObject<>();
            if (vnpResponseCode.equals("00")) {
                log.info("payment from VNpay success");
                //payment success
                //check order info xem là customer hay guest booking
                String sign3party = ThirdParty.THIRD_PARTY_MSG;
                if(vnpOrderInfo.contains(sign3party)){
                    log.info("order third party: " + vnpOrderInfo);
                    Order order = ordersRepository.findById(vnpOrderInfo.substring(sign3party.length(), vnpOrderInfo.indexOf(':'))).orElse(null);
                    if(order == null){
                        throw new Exception("order not found");
                    }
                    order.setStatus(StatusOrder.ACCEPT.toString());
                    ordersRepository.save(order);

                }else{ //customer
                    log.info("customer payment : " + vnpOrderInfo);
                    //khúc này có thể lưu DB or tăng số tiền đã nạp vào ví
                    int tmp = vnpOrderInfo.lastIndexOf(":");
                    String idCus = vnpOrderInfo.substring(tmp + 1).trim();
                    UserSystem userSystem = userServiceCall.getObject(idCus);
                    userSystem.setImgUrl(userSystem.getImgUrl().substring(linkBucket.length()));
                    if (userSystem != null) {
                        //cập nhật ví
                        String rs = paymentServiceCall.updateObjectBalance(WalletDTOupdate.builder()
                                .customerId(idCus)
                                .balance(Integer.parseInt(vnpAmount) / 100).build() //tại lúc đầu tạo * 100, nên giờ chia 100 cho đúng
                                );
                        if (!rs.equals(StatusResponse.SUCCESS.toString())) {
                            throw new Exception("update balance fail: " + rs);
                        }
                        //cập nhật transaction này
                        String formatvnp_PayDate = vnpPayDate.substring(0, 4) + "-" + vnpPayDate.substring(4, 6) + "-" + vnpPayDate.substring(6, 8)
                                + " " + vnpPayDate.substring(8, 10) + ":" + vnpPayDate.substring(10, 12) + ":" + vnpPayDate.substring(12, 14);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        rs = "";
                        rs = paymentServiceCall.createObject(WalletTransactionDTOcreate.builder()
                                .customerId(idCus).amount(Integer.parseInt(vnpAmount) / 100)
                                .description(vnpOrderInfo).createDate(format.parse(formatvnp_PayDate))
                                .build());
                        if (!rs.equals(StatusResponse.SUCCESS.toString())) {
                            throw new Exception("create transaction fail: " + rs);
                        }
                    }
                }

                //set up response object
                responseObject.setCode(200);
                if(vnpOrderInfo.contains("guestbooking:")){
                    responseObject.setMessage("Successfully-guest");
                }else{
                    responseObject.setMessage("Successfully");
                }
                responseObject.setData(vnpOrderInfo);
            } else {
                log.error("payment from VNpay fail");
                //check if order info contain third party -> we must delete this order b/c cus or guest buy by third party (vn pay) and don't pay or pay fail
                if(vnpOrderInfo.contains(ThirdParty.THIRD_PARTY_MSG)){
                    Order order = ordersRepository.findById(vnpOrderInfo.substring(ThirdParty.THIRD_PARTY_MSG.length(), vnpOrderInfo.indexOf(':'))).orElse(null);
                    if(Objects.requireNonNull(order).getOrderDetails() != null && order.getOrderDetails().size() > 0){
                        for(OrderDetail orderDetail: order.getOrderDetails()){
                            kafkaMessagePulisher.updateStockFood(FoodDTOupdate.builder().id(orderDetail.getFoodId()).stock(orderDetail.getAmount()).build());
                        }
                    }
                    ordersRepository.delete(order);
                }
                //payment fail
                responseObject.setCode(400);
                responseObject.setMessage("Failed");
                responseObject.setData(vnpOrderInfo);
            }
            return responseObject;
        } catch (Exception e) {
            return ResponseObject.builder().code(400).message("Exception payment: " + e.getMessage()).build();
        }
    }
}
