
```mermaid
classDiagram

class Fachada {
    - FachadaDonadoresYEntidades fachadaDonadoresYEntidades
    - FachadaDonaciones fachadaDonaciones
    - Map~String, Deposito~ depositos
    - Map~String, Asignacion~ asignacionesPorPaquete
    - AlgoritmoAsignacion algoritmo

    + agregarDeposito()
    + buscarDepositoPorID()
    + buscarAsignacionPorPaqueteID()
    + gestionarDonacion()
    + ejecutarMatchmaking()
    + reportarEntrega()
    + setAlgoritmoMM()
}

class AlgoritmoAsignacion {
    <<interface>>
    + elegir(List~NecesidadLogistica~)
}

class AlgoritmoPrioridadSubatendidos {
    + elegir(List~NecesidadLogistica~)
}

class NecesidadLogistica {
    - id : String
    - entidadId : String
    - cantidadObjetivo : int
}

class Deposito {
    - id : String
    - nombre : String 
    - direccion : String
    - capacidadMaxima : int
    - stockActual : int
    - listaDePaquetes : List<Paquete> 
}

class Asignacion {
    - id : String
    - paquete : Paquete
    - necesidadID : String
    - fecha : LocalDateTime
    - estado : EstadoAsginacionEnum
}

class Paquete {
    - paqueteID
    - donacionID
    - productoID
    - cantidad
}

class FachadaDonadoresYEntidades
class FachadaDonaciones

Fachada --> FachadaDonadoresYEntidades
Fachada --> FachadaDonaciones
Fachada --> AlgoritmoAsignacion
AlgoritmoAsignacion <|.. AlgoritmoPrioridadSubatendidos

Fachada --> Deposito
Fachada --> Asignacion
Fachada --> Paquete

AlgoritmoAsignacion --> NecesidadLogistica