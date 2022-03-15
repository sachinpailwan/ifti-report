package com.db.flare.flareifti.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Data
public class PaymentIn {

    private String transactionId;
    private String direction;
    private String orderingBic;
    private String beneficiaryBic;
    private String senderBic;
    private String receiverBic;
    private Double amount;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAYMENT_IN")
    @SequenceGenerator(sequenceName = "SEQ_T_PAYMENT_IN", allocationSize = 1, name = "SEQ_PAYMENT_IN")
    private Long paymentId;
}
