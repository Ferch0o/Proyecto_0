# Proyecto 0 — Dificultad progresiva para Minecraft

**Proyecto 0** es un plugin de supervivencia para **Minecraft Java Edition** que introduce desafíos por etapas. En lugar de sustituir por completo la experiencia vanilla desde el primer momento, el servidor incorpora criaturas modificadas, objetos especiales, nuevos comportamientos y eventos que hacen que la supervivencia sea progresivamente más exigente.

El proyecto se desarrolla sobre **Paper 1.21.5**, con **Java 21**, la **API de Paper/Bukkit** y **Gradle (Kotlin DSL)**. La implementación está organizada en sistemas independientes para facilitar el balance, las pruebas y la incorporación de futuras etapas.

> **Estado de esta documentación:** la etapa **días 7–10 (Beta 9)** está integrada en `main`. Las etapas 17–20, 25–30 y 35 son parte de la hoja de ruta y se documentarán cuando sus implementaciones estén terminadas. «Beta 9» identifica el documento de balance de esta etapa; el proyecto utiliza actualmente `1.0-SNAPSHOT` como versión de compilación.

## Contenido

- [¿Cómo funciona la dificultad progresiva?](#cómo-funciona-la-dificultad-progresiva)
- [Etapas del proyecto](#etapas-del-proyecto)
- [Características principales](#características-principales)
- [Requisitos y compilación](#requisitos-y-compilación)
- [Instalación y configuración](#instalación-y-configuración)
- [Comandos principales](#comandos-principales)
- [Estructura del código](#estructura-del-código)
- [Desarrollo y documentación](#desarrollo-y-documentación)

## ¿Cómo funciona la dificultad progresiva?

El plugin utiliza un **día lógico del servidor**, almacenado en `config.yml` mediante la clave `current-day`. Las diferentes funciones consultan ese valor a través de `DayManager`: por ejemplo, una característica del día 10 se habilita cuando `dayManager.isAtLeast(10)` es verdadero.

Esto permite probar y habilitar cada etapa de forma controlada. **En la implementación documentada, el número de día no se incrementa automáticamente por el mero paso del tiempo en Minecraft**; puede administrarse con `/setday` y se conserva en la configuración. Por tanto, no debe confundirse con el contador de jornadas que aparece en otras interfaces del juego.

La progresión prevista se divide en cuatro hitos:

| Etapa | Enfoque | Estado de la documentación |
|---|---|---|
| **Días 7–10** | Primer cambio de dificultad, nuevas criaturas, Estrella Corrupta, Ballesta Explosiva y expansión de la Tormenta | **Documentada — Beta 9** |
| **Días 17–20** | Próxima etapa de dificultad y balance | Pendiente de desarrollo/documentación |
| **Días 25–30** | Escalada posterior | Planificada |
| **Día 35** | Hito final previsto | Planificado |

## Etapas del proyecto

### Días 7–10 — Beta 9

Esta etapa introduce el **Zombie Gigante**, la **Estrella Corrupta** y la posibilidad de que la Tormenta comience naturalmente desde el día 7. A partir del día 10 se incorporan variantes de zombis, esqueletos, creepers y arañas, ajustes a criaturas existentes, materiales especiales y la **Ballesta Explosiva**.

La Tormenta también evoluciona: aplica bonificaciones a criaturas, aumenta la capacidad de aparición de monstruos, intenta generar enemigos adicionales y altera determinadas mecánicas de combate y exploración.

**[Leer la documentación completa de los días 7–10](docs/dia-07-10/README.md)**. Incluye estadísticas, recetas, probabilidades, estructura Java, decisiones de desarrollo y guía de pruebas.

### Días 17–20

Etapa siguiente. Se incorporará su README cuando el código y el balance estén definidos. No se documentan aquí mecánicas que todavía no están implementadas.

### Días 25–30

Etapa futura. Su documentación se publicará de manera independiente para conservar la evolución del proyecto sin mezclar versiones ni reglas de distintos hitos.

### Día 35

Hito final contemplado en la planificación actual. Tendrá su propio README al finalizar el desarrollo correspondiente.

## Características principales

- **Mobs personalizados sin sustituir toda la infraestructura de Minecraft:** se identifican mediante datos persistentes en las entidades y reciben atributos, equipamiento y habilidades específicas.
- **Objetos especiales:** objetos identificados mediante `PersistentDataContainer`, nombre y descripción personalizados, con identificadores `CustomModelData` preparados para futuras apariencias visuales.
- **Recetas con cantidades especiales por casilla:** la Estrella Corrupta y la Ballesta Explosiva tienen recetas y validaciones adicionales, más allá de los ingredientes estándar.
- **Tormenta progresiva:** duración acumulativa, activación natural a partir del día 7 y nuevas consecuencias desde el día 10.
- **Herramientas de prueba:** comandos de aparición de mobs y entrega de objetos, con permisos administrativos.
- **Separación de responsabilidades:** clases de dominio, listeners, tareas periódicas, registros y módulos `Bootstrap`.

> **Nota sobre recursos visuales:** el código reserva identificadores para modelos personalizados, pero eso **no significa que el repositorio incluya ya un paquete de recursos final**. Las texturas y modelos del lado del cliente se gestionan por separado.

## Requisitos y compilación

| Elemento | Requisito actual |
|---|---|
| Servidor | Paper **1.21.5** |
| Lenguaje / JDK | **Java 21** |
| Construcción | **Gradle Wrapper** incluido en el repositorio |
| Dependencia de compilación | `io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT` (`compileOnly`) |
| Tipo de proyecto | Plugin Java para servidor Paper/Bukkit |

Clona el proyecto y compílalo desde la carpeta raíz:

```bash
git clone https://github.com/Ferch0o/Proyecto_0.git
cd Proyecto_0
```

**Windows (PowerShell):**

```powershell
.\gradlew.bat clean build
```

**Linux/macOS:**

```bash
./gradlew clean build
```

El archivo JAR generado aparece en `build/libs/`. Con la configuración actual de Gradle, el artefacto principal se denomina normalmente `Proyecto_0-1.0-SNAPSHOT.jar`.

> La compilación demuestra que el código puede generarse como plugin; no sustituye las pruebas de funcionamiento de explosiones, IA, probabilidades y eventos en un servidor Paper real.

## Instalación y configuración

1. Instala y configura un servidor **Paper 1.21.5** ejecutándose con **Java 21**.
2. Compila el proyecto y copia su JAR a `plugins/`.
3. Inicia o reinicia completamente el servidor para cargar el plugin y crear su configuración.
4. Revisa `plugins/DifficultyPlugin/config.yml`. La configuración contiene `current-day` y datos persistentes de la Tormenta.
5. Usa `/day` para consultar la etapa lógica y `/setday <día>` para cambiarla como operador.
6. Prueba primero en un **mundo de desarrollo** antes de instalar una versión nueva en una partida importante.

**Ejemplo de configuración inicial:**

```yaml
current-day: 0

storm:
  active: false
  remaining-seconds: 0
  total-seconds: 0
  start-day: 0
```

La duración y el estado de la Tormenta se administran mediante `StormManager`. Si editas manualmente el archivo, realiza los cambios con el servidor detenido para evitar sobrescrituras.

## Comandos principales

| Comando | Descripción | Acceso |
|---|---|---|
| `/day` | Consulta el día lógico actual | General |
| `/setday <día>` | Cambia la etapa de progresión | `difficulty.setday` |
| `/storm start` / `/storm stop` | Control administrativo de la Tormenta | `difficulty.admin` |
| `/givecorruptedstar` | Entrega la Estrella Corrupta para pruebas | `difficulty.admin` |
| `/spawngiant` | Genera el Zombie Gigante para pruebas | `difficulty.admin` |
| `/giveexplosivecrossbow` | Entrega una Ballesta Explosiva auténtica del plugin | `difficulty.admin` |
| `/spawninfrazombie`, `/spawncorruptedzombie`, `/spawnscavenger` | Genera zombis personalizados | `difficulty.admin` |
| `/spawnelementalskeleton`, `/spawngladiator` | Genera esqueletos personalizados | `difficulty.admin` |
| `/spawnfuturisticcreeper` | Genera un Creeper Futurista | `difficulty.admin` |
| `/spawnspeedspider`, `/spawnabyssspider`, `/spawnweaver`, `/spawngoliath` | Genera arañas personalizadas | `difficulty.admin` |
| `/sharedcure add` | Funcionalidad de calidad de vida para descuentos de aldeanos curados | `difficulty.sharedcure.admin` |

Los permisos se declaran en `src/main/resources/plugin.yml` y, en la configuración actual, los administrativos tienen `default: op`. **Los comandos de prueba pueden respetar condiciones de etapa:** recibir un objeto no significa necesariamente poder utilizarlo antes del día correspondiente.

## Estructura del código

```text
Proyecto_0/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── src/main/
│   ├── resources/
│   │   ├── plugin.yml
│   │   └── config.yml
│   └── java/me/difficulty/
│       ├── DifficultyPlugin.java
│       ├── bootstrap/
│       │   ├── CoreBootstrap.java
│       │   ├── Day7Bootstrap.java
│       │   ├── Day10Bootstrap.java
│       │   ├── day10/...
│       │   └── storm/StormBootstrap.java
│       ├── core/
│       │   ├── day/
│       │   ├── items/
│       │   ├── mobs/
│       │   └── recipes/
│       ├── features/
│       │   ├── day7/
│       │   ├── day10/
│       │   ├── storm/
│       │   ├── pets/
│       │   └── qualityoflife/
│       └── commands/
└── docs/
    └── dia-07-10/
        └── README.md
```

La estructura `docs/` es la propuesta para acompañar la etapa ya integrada; no modifica la organización existente de `src/`.

La clase `DifficultyPlugin` coordina el inicio del plugin. Los módulos `Bootstrap` instancian y registran componentes, mientras `core/` aporta sistemas reutilizables y `features/` contiene las mecánicas concretas. Esta arquitectura permite trabajar en nuevas etapas sin acumular toda la lógica en una única clase.

## Desarrollo y documentación

Cada etapa se trabajará en una rama de desarrollo, se contrastará con su documento de balance y se integrará en `main` después de las verificaciones correspondientes. Posteriormente se publicará su README específico.

**Criterios de documentación:**

1. Describir el comportamiento realmente implementado, no funciones todavía imaginadas.
2. Separar valores de balance, decisiones técnicas y resultados de pruebas.
3. Anotar las limitaciones conocidas y evitar presentar una revisión de código como prueba dentro de Minecraft.
4. Conservar el README de cada etapa para poder revisar cómo cambió la dificultad con el tiempo.

Para comenzar: **[Documentación técnica y funcional de los días 7–10](docs/dia-07-10/README.md)**.

---

**Repositorio:** [Ferch0o/Proyecto_0](https://github.com/Ferch0o/Proyecto_0) · **Objetivo actual de compatibilidad:** Paper 1.21.5 y Java 21.
