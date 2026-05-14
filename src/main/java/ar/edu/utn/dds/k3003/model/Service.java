package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;

public class Service {

    public boolean esNecesidadAplicable(NecesidadMaterialDTO necesidad, Integer cantidadADonar) {
        if(necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) return true;
        return necesidad.cantidadObjetivo() <= cantidadADonar;
    }
}
