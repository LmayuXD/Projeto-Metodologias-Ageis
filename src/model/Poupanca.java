
package model;

public class Poupanca {
    //dados da caixinha

        private final Long id;      // mesmo ID da conta dona da poupança
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
            return this.saldo += valor;
        }

        public double sacar(double valor) {
            if (valor > saldo) {
                throw new IllegalArgumentException("Saldo insuficiente na poupança!");
            }
           return  this.saldo -= valor;
        }
    }
}