package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import org.springframework.stereotype.Service;

@Service
public class NecesidadService {

    public boolean esNecesidadAplicable(NecesidadMaterialDTO necesidad, Integer cantidadDonada) {
        if(necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) return true;
        else if (necesidad.tipo() == TipoNecesidadMaterialEnum.RECURRENTE) {
            //si lo que necesito es menos o a lo sumo igual de lo que me donaron
            return (necesidad.cantidadObjetivo() - necesidad.cantidadRecibida()) <= cantidadDonada;
            //capaz estaria bueno que desde donadores y entidades se haga esta logica de
            // cantidad no cubierta o actual - cantidad faltante
        }
        return true;

    }
}
