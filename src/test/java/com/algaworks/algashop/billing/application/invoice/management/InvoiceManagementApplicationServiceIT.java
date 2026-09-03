package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditCard.CreditCardTestDataBuilder;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCard;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
class InvoiceManagementApplicationServiceIT {

    @Autowired
    private InvoiceManagementApplicationService invoiceManagementApplicationService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @MockitoSpyBean
    private InvoicingService invoicingService;

    @Test
    public void shouldGenerateInvoiceWithACreditCardAsPayment() {
        UUID customerId = UUID.randomUUID();
        CreditCard creditCard = CreditCardTestDataBuilder.aCreditCard()
                .customerId(customerId)
                .build();
        creditCardRepository.saveAndFlush(creditCard);

        GenerateInvoiceInput input = GenerateInvoiceInputTestDataBuilder.anInput()
                .customerId(customerId)
                .build();

        input.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .method(PaymentMethod.CREDIT_CARD)
                        .creditCardId(creditCard.getId())
                .build()
        );

        UUID invoiceId = invoiceManagementApplicationService.generate(input);

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(input.getOrderId());

        Mockito.verify(invoicingService).issue(any(), any(), any(), any() );


    }

    @Test
    public void shouldGenerateInvoiceWithGateBalanceAsPayment() {
        UUID customerId = UUID.randomUUID();

        GenerateInvoiceInput input = GenerateInvoiceInputTestDataBuilder.anInput()
                .customerId(customerId)
                .build();

        input.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .method(PaymentMethod.GATEWAY_BALANCE)
                        .build()
        );

        UUID invoiceId = invoiceManagementApplicationService.generate(input);

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(input.getOrderId());

        Mockito.verify(invoicingService).issue(any(), any(), any(), any() );


    }

}