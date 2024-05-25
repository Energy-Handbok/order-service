package com.khaphp.orderservice.service;

import com.khaphp.common.constant.EmailDefault;
import com.khaphp.common.constant.Role;
import com.khaphp.common.dto.ResponseObject;
import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.dto.food.FoodDTOviewInOrtherEntity;
import com.khaphp.common.dto.noti.NotificationDTOcreate;
import com.khaphp.common.dto.payment.WalletDTOupdate;
import com.khaphp.common.dto.payment.WalletTransactionDTOcreate;
import com.khaphp.common.dto.usersystem.UserSystemDTOviewInOrtherEntity;
import com.khaphp.common.entity.Food;
import com.khaphp.common.entity.UserSystem;
import com.khaphp.common.entity.Wallet;
import com.khaphp.orderservice.call.food.FoodServiceCall;
import com.khaphp.orderservice.call.notiservice.NotiServiceCall;
import com.khaphp.orderservice.call.payment.PaymentServiceCall;
import com.khaphp.orderservice.call.userservice.UserServiceCall;
import com.khaphp.orderservice.constant.Method;
import com.khaphp.orderservice.constant.StatusOrder;
import com.khaphp.orderservice.constant.StatusResponse;
import com.khaphp.orderservice.dto.Order.OrderDTOcreate;
import com.khaphp.orderservice.dto.Order.OrderDTOupdate;
import com.khaphp.orderservice.dto.Order.OrderDTOviewDetail;
import com.khaphp.orderservice.dto.OrderDetail.OrderDetailDTOcreate;
import com.khaphp.orderservice.dto.OrderDetail.OrderDetailDToviewInOrtherEntity;
import com.khaphp.orderservice.entity.Order;
import com.khaphp.orderservice.entity.OrderDetail;
import com.khaphp.orderservice.entity.PaymentOrder;
import com.khaphp.orderservice.exception.ErrorFound;
import com.khaphp.orderservice.exception.ObjectNotFound;
import com.khaphp.orderservice.repo.OrderDetailRepository;
import com.khaphp.orderservice.repo.OrdersRepository;
import com.khaphp.orderservice.repo.PaymentOrderRepository;
import com.khaphp.orderservice.service.vnpay.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl implements OrdersService {
    public static final String OBJECT_NOT_FOUND = "object not found";
    public static final String SUCCESS = "Success";
    public static final String EXCEPTION_MSG = "Exception: ";
    public static final String FOUND_MSG = "Found";
    public static final String USER_NOT_FOUND_MSG = "user not found";
    public static final String ORDER_NOT_FOUND_MSG = "Order not found";
    private final OrdersRepository ordersRepository;
    private final PaymentServiceCall paymentServiceCall;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserServiceCall userServiceCall;
    private final FoodServiceCall foodServiceCall;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    private final NotiServiceCall notiServiceCall;
    private final PaymentService paymentService;

    @Value("${aws.s3.link_bucket}")
    private String linkBucket;


    @Override
    public ResponseObject<Object> getAll(int pageSize, int pageIndex) {
        Page<Order> objListPage = null;
        List<Order> objList = null;
        int totalPage = 0;
        //paging
        if(pageSize > 0 && pageIndex > 0){
            objListPage = ordersRepository.findAll(PageRequest.of(pageIndex - 1, pageSize));  //vì current page ở code nó start = 0, hay bên ngoài la 2pga đầu tiên hay 1
            if(objListPage != null){
                totalPage = objListPage.getTotalPages();
                objList = objListPage.getContent();
            }
        }else{ //get all
            objList = ordersRepository.findAll();
            pageIndex = 1;
        }
        return ResponseObject.builder()
                .code(200).message(SUCCESS)
                .pageSize(objList.size()).pageIndex(pageIndex).totalPage(totalPage)
                .data(objList)
                .build();
    }

    @Override
    public ResponseObject<Object> getDetail(String id) {
        try{
            Order object = ordersRepository.findById(id).orElse(null);
            if(object == null) {
                throw new ObjectNotFound(OBJECT_NOT_FOUND);
            }
            OrderDTOviewDetail dto = modelMapper.map(object, OrderDTOviewDetail.class);
            //get customer
            UserSystem customer = userServiceCall.getObject(object.getCustomerId());
            dto.setCustomerV(UserSystemDTOviewInOrtherEntity.builder()
                    .id(customer.getId())
                    .name(customer.getName())
                    .imgUrl(customer.getImgUrl())
                    .build());

            //get customer
            UserSystem employee = userServiceCall.getObject(object.getEmployeeId());
            dto.setEmployeeV(UserSystemDTOviewInOrtherEntity.builder()
                    .id(employee.getId())
                    .name(employee.getName())
                    .imgUrl(employee.getImgUrl())
                    .build());

            //get customer
            UserSystem shipper = userServiceCall.getObject(object.getShipperId());
            dto.setShipperV(UserSystemDTOviewInOrtherEntity.builder()
                    .id(shipper.getId())
                    .name(shipper.getName())
                    .imgUrl(shipper.getImgUrl())
                    .build());

            dto.setOrderDetailsV(new ArrayList<>());
            for (OrderDetail orderDetail : object.getOrderDetails()) {
                OrderDetailDToviewInOrtherEntity orderDetailDToviewInOrtherEntity = modelMapper.map(orderDetail, OrderDetailDToviewInOrtherEntity.class);
                //get food
                Food food = foodServiceCall.getDetail(orderDetail.getFoodId());
                FoodDTOviewInOrtherEntity foodDTOviewInOrtherEntity = modelMapper.map(food, FoodDTOviewInOrtherEntity.class);
                foodDTOviewInOrtherEntity.setImg(linkBucket + foodDTOviewInOrtherEntity.getImg());
                orderDetailDToviewInOrtherEntity.setFoodV(foodDTOviewInOrtherEntity);
                dto.getOrderDetailsV().add(orderDetailDToviewInOrtherEntity);
            }
            return ResponseObject.builder()
                    .code(200)
                    .message(FOUND_MSG)
                    .data(dto)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject<Object> create(OrderDTOcreate object) throws Exception {
        try{
            //check user
            UserSystem userSystem = validateUserInOrder(object);

            //create order trc
            Order order = createOrder(userSystem, object);
            order = ordersRepository.save(order);

            //nếu là wallet thì check balance để trừ tiền
            if(object.getMethod().equals(Method.WALLET.toString())){
                checkBalanceToMinus(order, userSystem, object);
            }

            //create order detail
            for (OrderDetailDTOcreate orderDetailDTOcreate : object.getOrderDetails()) {
                OrderDetail orderDetail = createOrderDetail(order, orderDetailDTOcreate);
                //cập nhật total price cho order
                order.setTotalPrice(order.getTotalPrice() + (orderDetail.getAmount() * orderDetail.getPrice()));
            }

            //create payment order
            createPaymentOrder(order, object);

            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .data(order)
                    .build();
        }catch (Exception e){
            throw new ErrorFound(EXCEPTION_MSG + e.getMessage());
        }
    }

    @Override
    public ResponseObject<Object> orderThirdParty(HttpServletRequest req, OrderDTOcreate object) throws Exception {
        try{
            //check user
            UserSystem userSystem = validateUserInOrder(object);

            //create order trc
            Order order = createOrder(userSystem, object);
            order = ordersRepository.save(order);

            //create order detail
            for (OrderDetailDTOcreate orderDetailDTOcreate : object.getOrderDetails()) {
                OrderDetail orderDetail = createOrderDetail(order, orderDetailDTOcreate);
                //cập nhật total price cho order
                order.setTotalPrice(order.getTotalPrice() + (orderDetail.getAmount() * orderDetail.getPrice()));
            }

            //create payment order
            createPaymentOrder(order, object);

            //gọi vn pay và nhận rs trả về để đổi status order
            ResponseObject urlRs = paymentService.createPayment(req, Math.round(order.getTotalPrice()), userSystem.getId(), true, order.getId());
            if(urlRs.getCode() != 200){
                throw new Exception(urlRs.getMessage());
            }

//            //chuyển hướng đến url payment của vn pay
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.LOCATION, (String) urlRs.getData());
//            return new ResponseEntity<>(headers, HttpStatus.FOUND);

            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .data((String) urlRs.getData())
                    .build();
        }catch (Exception e){
            throw new ErrorFound(EXCEPTION_MSG + e.getMessage());
        }
    }

    private UserSystem validateUserInOrder(OrderDTOcreate object) throws Exception{
        UserSystem userSystem = null;
        if(object.getPhoneGuest() != null){
            //guest order COD
            userSystem = userServiceCall.getDetailByEmail(EmailDefault.CUSTOMER_EMAIL_DEFAULT);
        }else{
            //user order
            userSystem =  userServiceCall.getObject(object.getCustomerId());
        }
        if(userSystem == null){
            throw new ObjectNotFound(USER_NOT_FOUND_MSG);
        }
        return userSystem;
    }

    private void createPaymentOrder(Order order, OrderDTOcreate object) throws Exception{
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrder(order);
        paymentOrder.setMethod(object.getMethod());
        if(object.getPhoneGuest() != null){
            paymentOrder.setNameGuest(object.getNameGuest());
            paymentOrder.setPhoneGuest(object.getPhoneGuest());
            paymentOrder.setAddress(object.getAddress());
        }
        paymentOrderRepository.save(paymentOrder);
    }

    private OrderDetail createOrderDetail(Order order, OrderDetailDTOcreate orderDetailDTOcreate) throws Exception{
        OrderDetail orderDetail = modelMapper.map(orderDetailDTOcreate, OrderDetail.class);

        Food food = foodServiceCall.getDetail(orderDetailDTOcreate.getFoodId());
        if(food == null){
            throw new ObjectNotFound("food not found");
        }

        //check stock của food trogn kho xem còn đủ không
        if(food.getStock() < orderDetail.getAmount()){
            throw new Exception("stock not enough");
        }
//        orderDetail.setId(new OrderDetailKey(food.getId(), order.getId()));
        orderDetail.setFoodId(food.getId());
        orderDetail.setOrder(order);
        orderDetailRepository.save(orderDetail);

        //cập nhật lại stock cho food
        food.setStock(food.getStock() - orderDetail.getAmount());
        FoodDTOupdate foodDTOupdate = modelMapper.map(food, FoodDTOupdate.class);
        String response = foodServiceCall.update(foodDTOupdate);
        if(!response.equals(StatusResponse.SUCCESS.toString())){
            log.error("update food error: " + response);
        }else{
            log.info("update food success: " + response + "|" + foodDTOupdate.toString());
        }

        return orderDetail;
    }

    private void checkBalanceToMinus(Order order, UserSystem userSystem, OrderDTOcreate object) throws Exception {
        int totalPrice = 0;
        //tính totalprice trước để check tiền trừ
        for (OrderDetailDTOcreate orderDetailDTOcreate : object.getOrderDetails()) {
            totalPrice += orderDetailDTOcreate.getPrice() * orderDetailDTOcreate.getAmount();
        }
        Wallet wallet = paymentServiceCall.detailObject(userSystem.getId());
        if(wallet.getBalance() < totalPrice){
            throw new Exception("balance not enough");
        }else{
            //thanh toán tiền
//            wallet.setBalance(wallet.getBalance() - totalPrice);
            WalletDTOupdate walletDTOupdate = WalletDTOupdate.builder().customerId(userSystem.getId()).balance(totalPrice * (-1)).build();
//            userSystemRepository.save(userSystem);
            try{
                String response = paymentServiceCall.updateObjectBalance(walletDTOupdate);
                if(!response.equals(StatusResponse.SUCCESS.toString())){
                    throw new Exception("update wallet fail: " + response);
                }else{
                    log.info("update wallet success: " + response +  "|" + walletDTOupdate.toString());
                }
            }catch (Exception e){
                throw new Exception("update wallet error: " + e.getMessage());
            }
            //create wallet transaction luôn
            try{
                String response = paymentServiceCall.createObject(WalletTransactionDTOcreate.builder()
                        .customerId(object.getCustomerId())
                        .amount(-totalPrice).description("Thanh toan don hang " +order.getId())
                        .createDate(new Date(System.currentTimeMillis()))
                        .build());
                if(!response.contains(StatusResponse.SUCCESS.toString())){
                    throw new Exception("create transaction fail: " + response);
                }else{
                    log.info("create transaction success: " + response);
                }
            }catch (Exception e){
                throw new Exception("create transaction error: " + e.getMessage());
            }
        }
    }

    private Order createOrder(UserSystem userSystem, OrderDTOcreate object) throws Exception {
        Order order = new Order();
        order.setCreateDate(new Date(System.currentTimeMillis()));
        order.setCustomerId(userSystem.getId());

        //set default employee
        UserSystem employee = userServiceCall.getDetailByEmail(EmailDefault.EMPLOYEE_EMAIL_DEFAULT);
        if(employee == null){
            throw new ObjectNotFound("default employee not found");
        }
        order.setEmployeeId(employee.getId());

        //set default shipper
        UserSystem shipper = userServiceCall.getDetailByEmail(EmailDefault.SHIPPER_EMAIL_DEFAULT);
        if(shipper == null){
            throw new ObjectNotFound("default shipper not found");
        }
        order.setShipperId(shipper.getId());

        if(     object.getMethod().equals(Method.COD.toString()) ||
                object.getMethod().equals(Method.THIRDPARTY.toString())){   //vì COD, THIRDPARTY mới cần check duyệt, còn cái kia là thanh toán đơn luôn rồi
            order.setStatus(StatusOrder.PENDING.toString());
        }else{
            order.setStatus(StatusOrder.ACCEPT.toString());
        }
        order.setUpdateDate(null);
        order.setDeliveryTime(null);
        return order;
    }

    @Override
    public ResponseObject<Object> updateStatus(OrderDTOupdate object) {
        try{
            Order object1 = ordersRepository.findById(object.getOrderId()).orElse(null);
            if(object1 == null) {
                throw new ObjectNotFound(ORDER_NOT_FOUND_MSG);
            }
            UserSystem employee = userServiceCall.getObject(object.getEmployeeId());
            if(employee == null){
                throw new ObjectNotFound("employee is not found");
            }
            object1.setStatus(object.getStatus());
            object1.setUpdateDate(new Date(System.currentTimeMillis()));
            if(     object.getStatus().equals(StatusOrder.REJECT.toString()) ||
                    object.getStatus().equals(StatusOrder.CANCEL.toString())){
                //trả lại stock cho food
                updateStockToFood(object1.getOrderDetails());
            }
            ordersRepository.save(object1);
            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseObject<Object> updateShipperTakeOrder(String orderId, String shipperId) {
        try{
            Order object1 = ordersRepository.findById(orderId).orElse(null);
            if(object1 == null) {
                throw new ObjectNotFound(ORDER_NOT_FOUND_MSG);
            }
            UserSystem shipper = userServiceCall.getObject(shipperId);
            if(shipper == null){
                throw new ObjectNotFound("shipper is not found");
            }
            if(!object1.getStatus().equals(StatusOrder.ACCEPT.toString())){
                throw new Exception("order must be accept");
            }
            object1.setStatus(StatusOrder.WAITING.toString());
            object1.setUpdateDate(new Date(System.currentTimeMillis()));
            object1.setShipperId(shipper.getId());
            ordersRepository.save(object1);

            //noti đến ch order để báo cho họ bik
            try{
                boolean rs = notiServiceCall.create(NotificationDTOcreate.builder()
                        .userId(object1.getCustomerId())
                        .title(shipper.getName() +" đã tiếp nhận đơn hàng " + orderId + " của bạn")
                        .build());
                if(!rs){
                    throw new Exception("create notification fail");
                }
            }catch (Exception e){
                throw new Exception("create notification error: " + e.getMessage());
            }

            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseObject<Object> finishDeilivery(String orderId, String shipperId) {
        try{
            Order object1 = ordersRepository.findById(orderId).orElse(null);
            if(object1 == null) {
                throw new ObjectNotFound(ORDER_NOT_FOUND_MSG);
            }
            UserSystem shipper = userServiceCall.getObject(shipperId);
            if(shipper == null){
                throw new ObjectNotFound("shipper is not found");
            }
            if(!object1.getStatus().equals(StatusOrder.WAITING.toString())){
                throw new Exception("order must be WAITING");
            }
            object1.setStatus(StatusOrder.FINISH.toString());
            object1.setUpdateDate(new Date(System.currentTimeMillis()));
            object1.setDeliveryTime(new Date(System.currentTimeMillis()));
            object1.setShipperId(shipper.getId());
            ordersRepository.save(object1);
            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseObject<Object> cancelOrder(String orderId, String userId, String role) {
        try{
            Order object1 = ordersRepository.findById(orderId).orElse(null);
            if(object1 == null) {
                throw new ObjectNotFound(ORDER_NOT_FOUND_MSG);
            }
            //valid role
            List<String> roleCheck = List.of(Role.CUSTOMER.toString(), Role.SHIPPER.toString(), Role.EMPLOYEE.toString());
            if(!roleCheck.contains(role)){
                throw new Exception("role is not valid, must be CUSTOMER/ SHIPPER or EMPLOYEE");
            }

            //valid user
            UserSystem userSystem = userServiceCall.getObject(userId);
            if(userSystem == null){
                throw new ObjectNotFound("user is not found");
            }
            if(!roleCheck.contains(userSystem.getRole())){
                throw new Exception("role is not valid, must be CUSTOMER/ SHIPPER or EMPLOYEE");
            }
            if(role.equals(Role.CUSTOMER.toString())){
                if (!object1.getCustomerId().equals(userSystem.getId())) {    //check xem order phải của cus này ko
                    throw new Exception("user is not own this order");
                }
                if(     object1.getStatus().equals(StatusOrder.ACCEPT.toString()) ||
                        object1.getStatus().equals(StatusOrder.WAITING.toString()) ||
                        object1.getStatus().equals(StatusOrder.FINISH.toString())){     //nếu order đã ACCPET thì ko CANCEL đc
                    throw new Exception("order can not cancel");
                }
            }else if(role.equals(Role.SHIPPER.toString())){
                if (!object1.getShipperId().equals(userSystem.getId())) {    //check xem order phải của shipper này ko
                    throw new Exception("shipper is not own this order");
                }
            }
            //còn employee thì đứa nào cancel cũng đc
            object1.setStatus(StatusOrder.CANCEL.toString());
            object1.setUpdateDate(new Date(System.currentTimeMillis()));
            //trả lại stock cho food
            updateStockToFood(object1.getOrderDetails());

            ordersRepository.save(object1);
            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }

    private void updateStockToFood(List<OrderDetail> orderDetails) throws Exception {
        for (OrderDetail orderDetail: orderDetails){
            //get food ra để update lại stock
            Food food = foodServiceCall.getDetail(orderDetail.getFoodId());
            food.setStock(food.getStock() + orderDetail.getAmount());

            //update lại db
            try{
                FoodDTOupdate foodDTOupdate = modelMapper.map(food, FoodDTOupdate.class);
                String rs = foodServiceCall.update(foodDTOupdate);
                if(!rs.equals(StatusResponse.SUCCESS.toString())){
                    throw new Exception("update food fail: " + rs);
                }
            }catch (Exception e){
                throw new Exception("update food error: " + e.getMessage());
            }
        }
    }

    @Override
    public ResponseObject<Object> delete(String id) {
        try{
            Order object = ordersRepository.findById(id).orElse(null);
            if(object == null) {
                throw new Exception(OBJECT_NOT_FOUND);
            }
            //delete order detail
            orderDetailRepository.deleteAll(object.getOrderDetails());
            //delete payment order
            paymentOrderRepository.delete(object.getPaymentOrder());

            //delete order
            ordersRepository.delete(object);
            return ResponseObject.builder()
                    .code(200)
                    .message(SUCCESS)
                    .build();
        }catch (Exception e){
            return ResponseObject.builder()
                    .code(400)
                    .message(EXCEPTION_MSG + e.getMessage())
                    .build();
        }
    }
}
