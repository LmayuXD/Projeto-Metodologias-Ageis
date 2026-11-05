
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Poupanca {
    // dados da caixinha

    private final Long id; // mesmo ID da conta dona da poupança
    private double saldo;

    public Poupanca(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public double getSaldo() {
        return saldo;
    }

    public double depositar(double valor) {
        this.saldo = BigDecimal.valueOf(this.saldo)
                .add(BigDecimal.valueOf(valor))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return this.saldo;
    }

    public double sacar(double valor) {
        if (valor > saldo) {
            throw new IllegalArgumentException("Saldo insuficiente na poupança!");
        }

        this.saldo = BigDecimal.valueOf(this.saldo)
                .subtract(BigDecimal.valueOf(valor))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return this.saldo;
    }
}
