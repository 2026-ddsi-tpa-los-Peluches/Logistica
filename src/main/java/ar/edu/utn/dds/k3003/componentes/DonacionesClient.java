package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Slf4j
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

            Map<String, String> body = Map.of("estado", estado.name());
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            log.info("Cambiar estado de donacion: donacionID={} estado={}",donacionID,estado);

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

    public DonacionDTO obtenerDonacionPorId(String donacionID) {
        try {
            // 1. Armamos la URL apuntando al endpoint GET del microservicio de donaciones
            String url = baseUrl + "/donaciones/" + donacionID;

            // 2. Configuramos los headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);


            // 4. Ejecutamos la petición GET esperando un DonacionDTO de respuesta
            ResponseEntity<DonacionDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    DonacionDTO.class
            );

            // 5. Retornamos el body si la respuesta fue exitosa
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            // Manejo específico si el microservicio devuelve 404 Not Found
            throw new NoSuchElementException("No se encontró la donación con ID: " + donacionID);
        } catch (Exception e) {
            // Manejo general de errores de red o del servidor remoto
            throw new RuntimeException("Error al comunicarse con el microservicio de donaciones: " + e.getMessage(), e);
        }
    }
}