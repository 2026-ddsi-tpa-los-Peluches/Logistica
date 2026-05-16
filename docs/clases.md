
```mermaid
classDiagram

class Fachada {
    - FachadaDonadoresYEntidades fachadaDonadoresYEntidades
    - FachadaDonaciones fachadaDonaciones
    - Map~String, Deposito~ depositos
    - Map~String, Asignacion~ asignacionesPorPaquete
    - AlgoritmoAsignacion algoritmo

    + agregarDeposito(DepositoDTO depositoDTO)
    + buscarDepositoPorID(String depositoID)
    + buscarAsignacionPorPaqueteID(String paqueteID)
    + gestionarDonacion(String depositoID, String DonacionID, String ProductoID, Integer cantidad)
    + ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades)
    + reportarEntrega(PaqueteDTO paqueteDTO)
    + setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo)
   
    + obtenerDepositos()
    + borrarDepositoPorID(String id)
    + buscarAsignacionPorID(String id)
    + buscarPaquetePorID(String id)
    
}

class AlgoritmoAsignacion {
    <<interface>>
    + elegir(List~NecesidadLogistica~, Integeer cantidadDonada)
}

class AlgoritmoPrioridadSubatendidos {
    + elegir(List~NecesidadLogistica~, Integeer cantidadDonada)
}

class AlgoritmoPrioridadPorScore {
    + elegir(List~NecesidadLogistica~, Integeer cantidadDonada)
}

class AlgoritmoFactory {
    + crear(TipoAlgoritmoEnum)
}

class NecesidadLogistica {
    - id : String
    - entidadId : String
    - cantidadObjetivo : int
}

class Deposito {
    - id : String
    - algoritmo : TipoAlgoritmoEnum
    - nombre : String 
    - direccion : String
    - capacidadMaxima : int
    - stockActual : List<Paquete>
}

class Asignacion {
    - id : String
    - paqueteId : String
    - necesidadId : String
    - fecha : LocalDateTime
    - estado : EstadoAsginacionEnum
    - historialEstadoAsignaciones : List<HistorialEstadoAsignacion>
}

class Paquete {
    - id : String
    - donacionID : String
    - productoID : String
    - cantidad : int
}

class HistorialEstadoAsignacion {
    - asigacionID : String
    - estado : EstadoAsignacionEnum
    - fecha : LocalDateTime 
}

class NecesidadService{
    +esNecesidadAplicable(NecesidadMaterialDTO necesidad, Integeer cantidadADonar)
}

class FachadaDonadoresYEntidades
class FachadaDonaciones

Fachada --> NecesidadService
Asignacion --> HistorialEstadoAsignacion
Fachada --> AlgoritmoFactory
Fachada --> FachadaDonadoresYEntidades
Fachada --> FachadaDonaciones
Fachada --> AlgoritmoAsignacion
AlgoritmoAsignacion <|.. AlgoritmoPrioridadSubatendidos
AlgoritmoAsignacion <|.. AlgoritmoPrioridadPorScore
Fachada --> Deposito
Fachada --> Asignacion
Fachada --> Paquete

Deposito --> Paquete
AlgoritmoFactory --> AlgoritmoAsignacion
AlgoritmoFactory --> AlgoritmoPrioridadSubatendidos
AlgoritmoFactory --> AlgoritmoPrioridadPorScore

AlgoritmoAsignacion --> NecesidadLogistica