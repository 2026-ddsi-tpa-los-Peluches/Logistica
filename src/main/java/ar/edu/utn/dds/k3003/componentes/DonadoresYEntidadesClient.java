package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.util.*;

@Slf4j
@Service
public class DonadoresYEntidadesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DonadoresYEntidadesClient(@Value("${url.donadoresYEntidades}")String baseUrl) {

        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    // GET /necesidades/{productoSolicitadoID}
    public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(
            String productoSolicitadoID
    ){
        try {


            String url = baseUrl + "/necesidades/" + productoSolicitadoID;
            NecesidadMaterialDTO[] necesidades =
                    restTemplate.getForObject(
                            url,
                            NecesidadMaterialDTO[].class
                    );

            return necesidades != null
                    ? Arrays.asList(necesidades)
                    : Collections.emptyList();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al consultar necesidades en DonadoresYEntidades",
                    e
            );
        }
    }
    // POST necesidades/{necesidadID}/satisfaccion

    public NecesidadMaterialDTO satisfacerNecesidad(
            String necesidadID,
            Integer cantidad
    ) {

        try{
            String url = baseUrl + "/necesidades/" + necesidadID + "/satisfaccion";

            Map<String, Integer> request = new HashMap<>();

            request.put("cantidad", cantidad);


            return restTemplate.postForObject(
                    url,
                    request,
                    NecesidadMaterialDTO.class
            );

        } catch (Exception e) {
            log.error("Error al satisfacer necesidad: necesidadID={} cantidad={}",necesidadID,cantidad);
            throw new RuntimeException(
                    "Error al satisfacer necesidad",
                    e
            );
        }
    }

    // GET /detalle/{id}
    public NecesidadMaterialDTO obtenerNecesidadPorId(String necesidadID) {
        try {
            String url = baseUrl + "/necesidades/detalle/" + necesidadID;

            return restTemplate.getForObject(
                    url,
                    NecesidadMaterialDTO.class
            );

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Error necesidad no existente: necesidadID={}",necesidadID);
            throw new NoSuchElementException("No se encontró la necesidad con ID: " + necesidadID);
        } catch (Exception e) {
            log.error("Error al consultar datos de necesidad a donadoresYEntidades: necesidadID={}",necesidadID);
            throw new RuntimeException("Error al consultar detalle de necesidad en DonadoresYEntidades", e);
        }
    }

    // GET /entidades/{id}
    public EntidadBeneficaDTO obtenerEntidadPorId(Integer entidadId) {
        try {
            String url = baseUrl + "/entidades/" + entidadId; // Ajustá la ruta base según el endpoint exacto que exponga el controller de tu compañero

            return restTemplate.getForObject(
                    url,
                    EntidadBeneficaDTO.class
            );

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Error entidad no existente: entidadId={}", entidadId);
            throw new NoSuchElementException("No se encontró la entidad benéfica con ID: " + entidadId);
        } catch (Exception e) {
            log.error("Error al consultar datos de entidad a donadoresYEntidades: entidadId={}", entidadId);
            throw new RuntimeException("Error al consultar entidad en DonadoresYEntidades", e);
        }
    }

}
