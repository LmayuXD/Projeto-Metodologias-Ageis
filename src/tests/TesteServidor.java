package tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import server.Servidor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TesteServidor {

    static final String BASE = "http://localhost:8080";
    static final HttpClient http = HttpClient.newHttpClient();
    static final ObjectMapper json = new ObjectMapper();
    public static int ok = 0, falhas = 0;

    record Resp(int status, JsonNode corpo, HttpResponse<String> bruta) {}

    public static void main(String[] args) throws Exception {
        Servidor.main(new String[0]);   // sobe o servidor (a porta 8080 precisa estar livre)

        testeCors();
        testeContas();
        testePoupanca();
        testePix();

        System.out.println("\n==== " + ok + " OK | " + falhas + " FALHAS ====");
        System.exit(falhas > 0 ? 1 : 0);   // encerra também o servidor
    }

    // ------------------------------------------------------------------ CORS
    public static void testeCors() throws Exception {
        titulo("CORS / preflight");

        Resp r = enviar("OPTIONS", "/api/contas", null);
        check("OPTIONS retorna 204", r.status() == 204);
        check("OPTIONS libera a origem (Access-Control-Allow-Origin)",
                r.bruta().headers().firstValue("Access-Control-Allow-Origin").orElse("").equals("*"));

        Resp g = enviar("GET", "/api/contas", null);
        check("respostas normais também têm CORS",
                g.bruta().headers().firstValue("Access-Control-Allow-Origin").orElse("").equals("*"));
        check("Content-Type é JSON",
                g.bruta().headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
    }

    // ---------------------------------------------------------------- contas
    public static void testeContas() throws Exception {
        titulo("/api/contas (RF-02.1)");

        Resp r = enviar("GET", "/api/contas", null);
        check("lista começa vazia", r.status() == 200 && r.corpo().isArray() && r.corpo().size() == 0);

        r = enviar("POST", "/api/contas", "{\"nomeTitular\":\"Maria Silva\",\"documento\":\"12345678901\"}");
        check("POST cria conta -> 201", r.status() == 201);
        check("conta 1 com nome e documento corretos",
                r.corpo().get("id").asLong() == 1
                        && r.corpo().get("nomeTitular").asText().equals("Maria Silva")
                        && r.corpo().get("documento").asText().equals("12345678901"));
        check("conta nasce com saldoInicial 500", r.corpo().get("saldoInicial").asDouble() == 500.0);

        r = enviar("POST", "/api/contas", "{\"nomeTitular\":\"João Pedro\",\"documento\":\"98765432100\"}");
        check("segunda conta tem id 2 (e aceita acento)",
                r.status() == 201 && r.corpo().get("id").asLong() == 2
                        && r.corpo().get("nomeTitular").asText().equals("João Pedro"));

        r = enviar("POST", "/api/contas",
                "{\"nomeTitular\":\"Ana Clara\",\"documento\":\"11122233344\",\"saldoInicial\":99999}");
        check("campos extras (saldoInicial) enviados pelo cliente são ignorados",
                r.status() == 201 && r.corpo().get("saldoInicial").asDouble() == 500.0);

        r = enviar("GET", "/api/contas", null);
        check("lista tem 3 contas", r.status() == 200 && r.corpo().size() == 3);

        r = enviar("GET", "/api/contas/1", null);
        check("GET /api/contas/1 -> 200 com os dados",
                r.status() == 200 && r.corpo().get("nomeTitular").asText().equals("Maria Silva"));

        r = enviar("GET", "/api/contas/99", null);
        check("GET conta inexistente -> 404 com campo erro", r.status() == 404 && r.corpo().has("erro"));

        r = enviar("GET", "/api/contas/abc", null);
        check("GET id não numérico -> 400", r.status() == 400 && r.corpo().has("erro"));

        r = enviar("POST", "/api/contas", "{\"nomeTitular\":\"A1\",\"documento\":\"12345678901\"}");
        check("nome inválido -> 400 com mensagem de validação",
                r.status() == 400 && r.corpo().get("erro").asText().contains("nome"));

        r = enviar("POST", "/api/contas", "{\"nomeTitular\":\"Carlos Lima\",\"documento\":\"123\"}");
        check("documento inválido -> 400", r.status() == 400 && r.corpo().has("erro"));

        r = enviar("POST", "/api/contas", "{\"nomeTitular\":\"\",\"documento\":\"\"}");
        check("campos vazios -> 400", r.status() == 400);

        r = enviar("POST", "/api/contas", "{\"documento\":\"12345678901\"}");
        check("nome ausente -> 400", r.status() == 400 && r.corpo().has("erro"));

        r = enviar("POST", "/api/contas", "{isso não é json");
        check("JSON malformado -> 400 \"JSON inválido\"",
                r.status() == 400 && r.corpo().get("erro").asText().equals("JSON inválido"));

        r = enviar("POST", "/api/contas", "");
        check("corpo vazio -> 400", r.status() == 400);

        r = enviar("GET", "/api/contas", null);
        check("requisições inválidas não criaram contas (continua com 3)", r.corpo().size() == 3);

        r = enviar("DELETE", "/api/contas/1", null);
        check("método não suportado -> 404", r.status() == 404);
    }

    // -------------------------------------------------------------- poupança
    public static void testePoupanca() throws Exception {
        titulo("/api/poupanca (RF-02.5)");

        Resp r = enviar("GET", "/api/poupanca/1", null);
        check("caixinha nasce com saldo 0",
                r.status() == 200 && r.corpo().get("saldo").asDouble() == 0.0
                        && r.corpo().get("id").asLong() == 1);

        r = enviar("POST", "/api/poupanca/1/guardar", "{\"valor\":200}");
        check("guardar 200 -> caixinha = 200", r.status() == 200 && r.corpo().get("saldo").asDouble() == 200.0);
        check("conta passou a ter 300", saldoConta(1) == 300.0);

        r = enviar("POST", "/api/poupanca/1/resgatar", "{\"valor\":50}");
        check("resgatar 50 -> caixinha = 150", r.status() == 200 && r.corpo().get("saldo").asDouble() == 150.0);
        check("conta passou a ter 350", saldoConta(1) == 350.0);

        r = enviar("GET", "/api/poupanca/1", null);
        check("GET reflete o saldo atual da caixinha", r.corpo().get("saldo").asDouble() == 150.0);

        r = enviar("POST", "/api/poupanca/1/guardar", "{\"valor\":99999}");
        check("guardar mais que o saldo -> 400 \"Saldo insuficiente\"",
                r.status() == 400 && r.corpo().get("erro").asText().contains("Saldo insuficiente"));

        r = enviar("POST", "/api/poupanca/1/resgatar", "{\"valor\":99999}");
        check("resgatar mais que a caixinha -> 400",
                r.status() == 400 && r.corpo().get("erro").asText().contains("Saldo insuficiente"));

        r = enviar("POST", "/api/poupanca/1/guardar", "{\"valor\":-5}");
        check("valor negativo -> 400", r.status() == 400);

        r = enviar("POST", "/api/poupanca/1/guardar", "{\"valor\":0}");
        check("valor zero -> 400", r.status() == 400);

        r = enviar("POST", "/api/poupanca/1/guardar", "{}");
        check("valor ausente -> 400", r.status() == 400);

        r = enviar("POST", "/api/poupanca/1/guardar", "{\"valor\":\"abc\"}");
        check("valor não numérico -> 400", r.status() == 400);

        r = enviar("POST", "/api/poupanca/1/guardar", "{nao e json");
        check("JSON malformado -> 400", r.status() == 400);

        check("operações inválidas não alteraram os saldos",
                saldoConta(1) == 350.0 && saldoCaixinha(1) == 150.0);

        r = enviar("POST", "/api/poupanca/99/guardar", "{\"valor\":10}");
        check("guardar em conta inexistente -> 400", r.status() == 400 && r.corpo().has("erro"));

        r = enviar("GET", "/api/poupanca/99", null);
        check("consultar conta inexistente -> 400", r.status() == 400);

        r = enviar("GET", "/api/poupanca/abc", null);
        check("id não numérico -> 400", r.status() == 400);

        r = enviar("GET", "/api/poupanca", null);
        check("sem id -> 404", r.status() == 404);

        r = enviar("POST", "/api/poupanca/1/transferir", "{\"valor\":10}");
        check("ação desconhecida -> 404", r.status() == 404);

        r = enviar("GET", "/api/poupanca/1/guardar", null);
        check("GET em rota de ação (só aceita POST) -> 404", r.status() == 404);

        check("caixinha de outra conta é independente", saldoCaixinha(2) == 0.0 && saldoConta(2) == 500.0);

        // esvazia tudo e confere que o dinheiro total é conservado
        enviar("POST", "/api/poupanca/1/resgatar", "{\"valor\":150}");
        check("resgate total devolve tudo (conta = 500, caixinha = 0)",
                saldoConta(1) == 500.0 && saldoCaixinha(1) == 0.0);
    }

    // ------------------------------------------------------- pix e extrato
    // Roda depois de testeContas e testePoupanca: contas 1, 2 e 3 existem, todas com saldo 500.
    //   1 = Maria Silva (12345678901) | 2 = João Pedro (98765432100) | 3 = Ana Clara (11122233344)
    public static void testePix() throws Exception {
        titulo("/api/pix e /api/contas/{id}/extrato (RF-02.3 e RF-02.4)");

        Resp r = enviar("GET", "/api/contas/1", null);
        check("JSON da conta não inclui o extrato", r.status() == 200 && !r.corpo().has("extrato"));

        r = enviar("GET", "/api/contas/1/extrato", null);
        check("extrato começa vazio", r.status() == 200 && r.corpo().isArray() && r.corpo().size() == 0);

        // ---- Pix válido
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "150", "almoço"));
        check("Pix 1 -> 2 de 150 retorna 201", r.status() == 201);
        check("comprovante é o registro de saída",
                r.corpo().get("tipo").asText().equals("PIX_ENVIADO") && r.corpo().get("valor").asDouble() == 150.0);
        check("comprovante tem id e data", r.corpo().get("id").asText().length() > 0 && r.corpo().get("data").asText().length() > 0);
        check("descrição cita o destinatário e a mensagem",
                r.corpo().get("descricao").asText().contains("João Pedro") && r.corpo().get("descricao").asText().contains("almoço"));
        String idPix = r.corpo().get("id").asText();

        check("origem foi debitada (350)", saldoConta(1) == 350.0);
        check("destino foi creditado (650)", saldoConta(2) == 650.0);

        r = enviar("GET", "/api/contas/1/extrato", null);
        check("extrato da origem tem 1 saída",
                r.status() == 200 && r.corpo().size() == 1 && r.corpo().get(0).get("tipo").asText().equals("PIX_ENVIADO"));
        r = enviar("GET", "/api/contas/2/extrato", null);
        check("extrato do destino tem 1 entrada com o mesmo id",
                r.corpo().size() == 1 && r.corpo().get(0).get("tipo").asText().equals("PIX_RECEBIDO")
                        && r.corpo().get(0).get("id").asText().equals(idPix));

        // ---- chave formatada e sem descrição
        r = enviar("POST", "/api/pix", corpoPix(3, "123.456.789-01", "10.5", null));
        check("chave com pontos e traço é aceita, descrição é opcional", r.status() == 201);
        check("saldos após o segundo Pix (3 = 489.5, 1 = 360.5)", saldoConta(3) == 489.5 && saldoConta(1) == 360.5);

        // ---- extrato: ordem e filtro
        r = enviar("GET", "/api/contas/1/extrato", null);
        check("extrato da conta 1 tem 2 registros, o mais recente primeiro",
                r.corpo().size() == 2 && r.corpo().get(0).get("valor").asDouble() == 10.5
                        && r.corpo().get(1).get("valor").asDouble() == 150.0);

        r = enviar("GET", "/api/contas/1/extrato?tipo=PIX_RECEBIDO", null);
        check("filtro PIX_RECEBIDO traz só o recebido",
                r.status() == 200 && r.corpo().size() == 1 && r.corpo().get(0).get("valor").asDouble() == 10.5);
        r = enviar("GET", "/api/contas/1/extrato?tipo=pix_enviado", null);
        check("filtro PIX_ENVIADO (minúsculo também) traz só o enviado",
                r.status() == 200 && r.corpo().size() == 1 && r.corpo().get(0).get("valor").asDouble() == 150.0);
        r = enviar("GET", "/api/contas/1/extrato?tipo=OUTRO", null);
        check("filtro inválido -> 400", r.status() == 400 && r.corpo().has("erro"));

        // ---- Pix recusado (nada deve mudar)
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "1000", null));
        check("saldo insuficiente -> 400",
                r.status() == 400 && r.corpo().get("erro").asText().contains("Saldo insuficiente"));
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "6000", null));
        check("acima do limite por transferência -> 400", r.status() == 400 && r.corpo().has("erro"));
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "0", null));
        check("valor zero -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "-5", null));
        check("valor negativo -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "10.005", null));
        check("mais de 2 casas decimais -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", "{\"contaOrigemId\":1,\"chaveDestino\":\"98765432100\"}");
        check("valor ausente -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", corpoPix(1, "", "10", null));
        check("chave vazia -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", corpoPix(1, "12345678901", "10", null));
        check("Pix para a própria conta -> 400",
                r.status() == 400 && r.corpo().get("erro").asText().contains("própria conta"));
        r = enviar("POST", "/api/pix", corpoPix(1, "98765432100", "10", "x".repeat(141)));
        check("descrição com mais de 140 caracteres -> 400", r.status() == 400);
        r = enviar("POST", "/api/pix", "{isso não é json");
        check("JSON malformado -> 400", r.status() == 400 && r.corpo().get("erro").asText().equals("JSON inválido"));
        r = enviar("POST", "/api/pix", corpoPix(1, "00000000000", "10", null));
        check("chave inexistente -> 404", r.status() == 404 && r.corpo().get("erro").asText().contains("Chave Pix"));
        r = enviar("POST", "/api/pix", corpoPix(99, "98765432100", "10", null));
        check("conta de origem inexistente -> 404", r.status() == 404);

        check("Pix recusados não mexeram nos saldos",
                saldoConta(1) == 360.5 && saldoConta(2) == 650.0 && saldoConta(3) == 489.5);
        check("dinheiro total do banco é conservado (1500)",
                saldoConta(1) + saldoConta(2) + saldoConta(3) == 1500.0);
        r = enviar("GET", "/api/contas/1/extrato", null);
        check("Pix recusados não entraram no extrato", r.corpo().size() == 2);

        // ---- rotas e métodos
        r = enviar("GET", "/api/contas/99/extrato", null);
        check("extrato de conta inexistente -> 404", r.status() == 404 && r.corpo().has("erro"));
        r = enviar("GET", "/api/contas/abc/extrato", null);
        check("extrato com id não numérico -> 400", r.status() == 400);
        r = enviar("POST", "/api/contas/1/extrato", "{}");
        check("POST no extrato (só aceita GET) -> 404", r.status() == 404);
        r = enviar("GET", "/api/pix", null);
        check("GET em /api/pix (só aceita POST) -> 404", r.status() == 404);
        r = enviar("POST", "/api/pix/1", "{}");
        check("/api/pix/1 -> 404", r.status() == 404);
    }

    // --------------------------------------------------------------- helpers
    // monta o JSON do Pix; valor entra como texto para testar casas decimais; descricao null = omitida
    static String corpoPix(long origem, String chave, String valor, String descricao) {
        String d = descricao == null ? "" : ",\"descricao\":\"" + descricao + "\"";
        return "{\"contaOrigemId\":" + origem + ",\"chaveDestino\":\"" + chave + "\",\"valor\":" + valor + d + "}";
    }

    static double saldoConta(long id) throws Exception {
        return enviar("GET", "/api/contas/" + id, null).corpo().get("saldoInicial").asDouble();
    }

    static double saldoCaixinha(long id) throws Exception {
        return enviar("GET", "/api/poupanca/" + id, null).corpo().get("saldo").asDouble();
    }

    static Resp enviar(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, corpo == null
                        ? BodyPublishers.noBody()
                        : BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        HttpResponse<String> resp = http.send(b.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String tipo = resp.headers().firstValue("Content-Type").orElse("");
        JsonNode node = (resp.body() == null || resp.body().isBlank() || !tipo.startsWith("application/json"))
                ? null : json.readTree(resp.body());
        return new Resp(resp.statusCode(), node, resp);
    }

    static void titulo(String t) { System.out.println("\n--- " + t + " ---"); }

    static void check(String nome, boolean passou) {
        if (passou) { ok++; System.out.println("[OK]     " + nome); }
        else { falhas++; System.out.println("[FALHA]  " + nome); }
    }
}