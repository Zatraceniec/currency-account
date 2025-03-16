package org.pn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
class NbpExchangeRateService {

    private static final String USD_PATH_SEGMENT = "usd";
    private static final String FORMAT_JSON_PARAM = "?format=json";
    public static final String RATES_ARRAY_JSON_PATH = "rates";
    public static final String MID_EXCHANGE_RATE_JSON_PATH = "mid";

    @Value("${nbp.api.url:https://api.nbp.pl/api/exchangerates/rates/a/}")
    private String npbApiUrl;


    double getUSDExchangeRate() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = UriComponentsBuilder.fromUriString(npbApiUrl + USD_PATH_SEGMENT + FORMAT_JSON_PARAM).build().toUri();
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            return (double) ((Map<String, Object>) ((List<Object>) response.get(RATES_ARRAY_JSON_PATH)).getFirst()).get(MID_EXCHANGE_RATE_JSON_PATH);
        } catch (Exception e) {
            String errorMessage = "Failed to fetch exchange rate";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
