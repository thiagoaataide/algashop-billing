package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.DomainException;
import com.algaworks.algashop.billing.domain.model.IdGenerator;
import lombok.*;
import org.apache.tomcat.util.buf.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSettings {

    @EqualsAndHashCode.Include
    private UUID uuid;
    private UUID creditCardId;
    private String gatewayCode;
    private PaymentMehod paymentMethod;

    static PaymentSettings brandNew(PaymentMehod method, UUID creditCardId) {

        Objects.requireNonNull(method);

        if (method.equals(PaymentMehod.CREDIT_CARD)) {
            Objects.requireNonNull(creditCardId);
        }

        return new PaymentSettings(
                IdGenerator.generateTimeBasedUUID(),
                creditCardId,
                null,
                method);
    }

    void assignGetawayCode(String gatewayCode) {
        if (gatewayCode.isBlank()) {
            throw new IllegalArgumentException();
        }

        if (this.getGatewayCode() != null) {
            throw new DomainException("Gateway code already assigned");
        }

        setGatewayCode(gatewayCode);
    }
}
