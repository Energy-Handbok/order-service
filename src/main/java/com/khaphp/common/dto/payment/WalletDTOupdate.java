package com.khaphp.common.dto.payment;

import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class WalletDTOupdate {
    private String customerId;
    private int balance;
}