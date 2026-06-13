package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class DonacionesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DonacionesClient(@Value("${url.donaciones}") String baseUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // // PATCH /donaciones/{id}/estado"
    public DonacionDTO cambiarEstadoDeDonacion(
            String donacionID,
            EstadoDonacionEnum estado
    ) {
        try {
            String url = baseUrl + "/donaciones/" + donacionID + "/estado";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<EstadoDonacionEnum> request =
                    new HttpEntity<>(estado, headers);

            return restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    request,
                    DonacionDTO.class
            ).getBody();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al cambiar estado de la donación",
                    e
            );
        }
    }
}