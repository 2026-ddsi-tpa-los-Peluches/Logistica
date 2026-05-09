```mermaid
flowchart LR

    Logistica["Logistica\n(FachadaLogistica)"]
    Donadores["DonadoresYEntidades\n(FachadaDonadoresYEntidades)"]
    Donaciones["Donaciones\n(FachadaDonaciones)"]

    %% QUIEN USA A QUIEN
    Logistica -- usa --> Donadores
    Logistica -- usa --> Donaciones

    Donaciones -- usa --> Logistica