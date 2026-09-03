package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceManagementApplicationService {

    private final PaymentGatewayService paymentGatewayService;
    private final InvoicingService invoicingService;
    private final InvoiceRepository invoiceRepository;
    private final CreditCardRepository creditCardRepository;

    @Transactional
    public UUID generate(GenerateInvoiceInput input) {

        verifyCreditCardId(input.getPaymentSettings().getCreditCardId(), input.getCustomerId());

        Payer payer = convertToPayer(input.getPayer());

        Set<LineItem> items = convertToLineItems(input.getItems());

        Invoice invoice = invoicingService.issue(input.getOrderId(), input.getCustomerId(), payer, items);
        invoice.changePaymentSettings(input.getPaymentSettings().getMethod(), input.getPaymentSettings().getCreditCardId());

        invoiceRepository.saveAndFlush(invoice);

        return invoice.getId();

    }

    private Set<LineItem> convertToLineItems(Set<LineItemInput> itemsInput) {
        Set<LineItem> lineItems = new LinkedHashSet<>();
        int itemNumber = 1;
        for (LineItemInput itemInput : itemsInput) {
            lineItems.add(LineItem.builder()
                            .number(itemNumber)
                            .name(itemInput.getName())
                            .amount(itemInput.getAmount())
                    .build());
            itemNumber++;
        }

        return lineItems;
    }

    private Payer convertToPayer(PayerData payer) {
        AddressData addressData = payer.getAddress();

        return Payer.builder()
                .fullName(payer.getFullName())
                .document(payer.getDocument())
                .email(payer.getEmail())
                .phone(payer.getPhone())
                .address(Address.builder()
                        .city(addressData.getCity())
                        .state(addressData.getState())
                        .neighborhood(addressData.getNeighborhood())
                        .street(addressData.getStreet())
                        .number(addressData.getNumber())
                        .complement(addressData.getComplement())
                        .zipCode(addressData.getZipCode())
                        .build())
                .build();
    }


    private void verifyCreditCardId(UUID creditCardId, @NotNull UUID customerId) {
        if (creditCardId != null && !creditCardRepository.existsByIdAndCustomerId(creditCardId, customerId)) {
            throw new CreditCardNotFoundException();
        }
    }
}
