# Proyecto 0 — Documentación de desarrollo y balance: días 7–10

**Etapa:** días 7–10  
**Documento de balance:** `Dia 7 - 10 Version_Beta(9).txt`  
**Plataforma:** Paper 1.21.5 · Java 21 · Gradle  
**Implementación de referencia:** rama `main`, commit de integración [`5ee3d16`](https://github.com/Ferch0o/Proyecto_0/commit/5ee3d16eadeeb2999a54cd299126b8f6cf14e6e9)  
**Estado:** código integrado en `main`; resultados de ejecución sujetos a las pruebas descritas más adelante.

[← Volver al README principal](../../README.md)

## Índice

1. [Objetivo y alcance de la etapa](#1-objetivo-y-alcance-de-la-etapa)
2. [Cómo se activa el contenido](#2-cómo-se-activa-el-contenido)
3. [Día 7: el primer cambio de dificultad](#3-día-7-el-primer-cambio-de-dificultad)
4. [Día 10: zombis personalizados](#4-día-10-zombis-personalizados)
5. [Día 10: esqueletos personalizados](#5-día-10-esqueletos-personalizados)
6. [Día 10: Creeper Futurista](#6-día-10-creeper-futurista)
7. [Día 10: arañas personalizadas](#7-día-10-arañas-personalizadas)
8. [Día 10: golems, caballos y mascotas](#8-día-10-golems-caballos-y-mascotas)
9. [Nuevos materiales y Ballesta Explosiva](#9-nuevos-materiales-y-ballesta-explosiva)
10. [La Tormenta a partir del día 10](#10-la-tormenta-a-partir-del-día-10)
11. [Estructura de desarrollo y arquitectura Java](#11-estructura-de-desarrollo-y-arquitectura-java)
12. [Decisiones técnicas y evolución del balance](#12-decisiones-técnicas-y-evolución-del-balance)
13. [Comandos, pruebas y criterios de aceptación](#13-comandos-pruebas-y-criterios-de-aceptación)
14. [Estado de cierre y próximos pasos](#14-estado-de-cierre-y-próximos-pasos)

## 1. Objetivo y alcance de la etapa

Los días 7–10 representan la primera ampliación importante de la dificultad del servidor. La finalidad es que el jugador note un cambio concreto en la supervivencia, tanto por los nuevos enemigos como por la aparición de mecánicas que antes no existían.

El diseño se divide en **dos momentos**:

- **Día 7:** aparece una amenaza singular, el **Zombie Gigante**; se habilita la **Estrella Corrupta** y la **Tormenta** adquiere la posibilidad de comenzar naturalmente.
- **Día 10:** varias familias de criaturas reciben variantes y habilidades; se añaden materiales, recetas y la **Ballesta Explosiva**; además, la Tormenta aplica modificadores adicionales.

Las variantes del día 10 se implementan sobre entidades de Minecraft ya existentes. El plugin identifica a cada criatura personalizada y le aplica atributos, equipamiento o lógica propia. Esta decisión permite desarrollar una experiencia diferenciada en un servidor Paper sin exigir, por sí sola, que todos los jugadores instalen un mod de cliente.

**Convenciones utilizadas en este documento:**

- **20 puntos de salud = 10 corazones**; cada corazón equivale a dos puntos.
- En la API de pociones, amplificador `0` = nivel I; `1` = II; `2` = III, etc.
- Las probabilidades se indican para el **evento o selección que controla cada listener**. No representan necesariamente la proporción global observada en todos los mobs del mundo, porque también intervienen el tipo de spawn, el terreno, la disponibilidad de espacio y otros sistemas.
- **Beta 9** es la versión del *documento de balance*, no un identificador de versión de Minecraft ni una release etiquetada de GitHub.

## 2. Cómo se activa el contenido

La clase [`DayManager`](../../src/main/java/me/difficulty/core/day/DayManager.java) conserva un valor entero `current-day`, leído y guardado en `config.yml`. El método `isAtLeast(n)` permite comprobar si una mecánica está disponible desde cierto día.

| Desde | Cambios relevantes |
|---|---|
| Día 0 | Funcionamiento base del plugin, incluido el sistema de Tormenta asociado a muertes. |
| Día 7 | Zombie Gigante, Estrella Corrupta y tiradas naturales de Tormenta. |
| Día 10 | Variantes de zombis, esqueletos, creepers y arañas; cambios a otras criaturas; materiales, Ballesta y efectos adicionales de Tormenta. |

El contador lógico se puede consultar con `/day` y modificar administrativamente con `/setday <día>`. **En el código de esta etapa no se observa un proceso que avance automáticamente `current-day` al pasar una jornada vanilla**; es una decisión importante para administradores y pruebas.

Los listeners y tareas comprueban la etapa antes de aplicar sus efectos, y `RecipeUnlockManager` descubre u oculta recetas según el día.

## 3. Día 7: el primer cambio de dificultad

### 3.1. Zombie Gigante

El Zombie Gigante es una amenaza de gran tamaño que se diferencia de un zombi común no solo visualmente, sino también por su desplazamiento, persecución y ataque personalizados.

| Propiedad | Implementación |
|---|---|
| Aparición | **5% de intento** al generarse un zombi natural elegible |
| Entorno | **Superficie del mundo normal**; requiere suelo sólido y espacio vertical/horizontal seguro |
| Recompensa durante días 7, 8 y 9 | **80%** de probabilidad de añadir **4–8 fragmentos de netherita** (`NETHERITE_SCRAP`) |
| Desde el día 10 | El listener **deja de añadir** esos fragmentos como recompensa |
| Detección / persecución | IA propia; alcance de detección configurado en **32 bloques** |
| Ataque | Daño base personalizado de **70 puntos**, alcance de **4 bloques** y **2 segundos** entre golpes |

**Cómo se desarrolló:** Minecraft dispone de la entidad `Giant`, pero para obtener el comportamiento deseado fue necesario construir lógica específica en vez de depender de la IA ordinaria de un zombi. La solución separa tres responsabilidades:

1. [`GiantSpawnListener`](../../src/main/java/me/difficulty/features/day7/gigante/GiantSpawnListener.java): escucha apariciones naturales de zombis, verifica superficie, espacio y probabilidad, genera la entidad `Giant` personalizada y evita conservar simultáneamente el zombi original cuando el reemplazo es válido.
2. [`GiantAiTask`](../../src/main/java/me/difficulty/features/day7/gigante/GiantAiTask.java): controla el comportamiento de exploración y persecución, con límites de distancia y comprobaciones de terreno.
3. [`GiantCombatTask`](../../src/main/java/me/difficulty/features/day7/gigante/GiantCombatTask.java): controla los ataques a jugadores, su alcance y su enfriamiento individual.

[`GiantZombie`](../../src/main/java/me/difficulty/features/day7/gigante/GiantZombie.java) contiene la configuración básica de la entidad; [`GiantDropListener`](../../src/main/java/me/difficulty/features/day7/gigante/GiantDropListener.java) administra su recompensa. Las tareas de IA y combate se ejecutan periódicamente desde `Day7Bootstrap`.

**Consideración de diseño:** el 5% no obliga a que aparezca un Gigante cuando no existe espacio suficiente. Si falla la validación de seguridad, se descarta el reemplazo. Esto evita generar una entidad enorme dentro de cuevas o estructuras inadecuadas.

### 3.2. Estrella Corrupta

La Estrella Corrupta es un objeto activable destinado a mejorar temporalmente la movilidad del jugador con un riesgo asociado.

| Propiedad | Valor |
|---|---|
| Efecto principal | **Velocidad III** durante **30 segundos** |
| Tiempo de reutilización | **60 segundos** |
| Efecto negativo | **5%** de aplicar **Debilidad V** durante **60 segundos** |
| Disponibilidad | Desde el día **7** |

La implementación identifica el objeto por su ID personalizado y no únicamente por su material. [`CorruptedStarListener`](../../src/main/java/me/difficulty/features/day7/corruptedstar/CorruptedStarListener.java) administra el uso, los efectos y el cooldown. [`CorruptedStarCooldownPersistenceListener`](../../src/main/java/me/difficulty/features/day7/corruptedstar/CorruptedStarCooldownPersistenceListener.java) incorpora lógica para conservar el enfriamiento al desconectarse y reconectarse.

#### Receta de la Estrella Corrupta

Cada posición muestra **el material y la cantidad exigida en esa casilla**, no la cantidad total.

| | Columna 1 | Columna 2 | Columna 3 |
|---|---|---|---|
| **Fila 1** | 5 diamantes | 3 fragmentos resonantes (`ECHO_SHARD`) | 5 diamantes |
| **Fila 2** | 3 patas de conejo | 1 estrella del Nether | 32 ojos de araña fermentados |
| **Fila 3** | 5 diamantes | 3 fragmentos resonantes | 5 diamantes |

**Totales por una fabricación:** 20 diamantes, 6 fragmentos resonantes, 3 patas de conejo, 1 estrella del Nether y 32 ojos de araña fermentados.

Una receta estándar de Minecraft no expresa por sí sola todas esas cantidades por casilla. Por eso el sistema separa la **forma de la receta** (`CorruptedStarRecipe`) de las **comprobaciones y consumo de materiales** (`CorruptedStarRecipeListener`). También se integra con el libro de recetas a través de `CorruptedStarRecipeBookListener`.

### 3.3. Tormenta natural desde el día 7

Antes de esta modificación, el sistema de Tormenta ya podía recibir tiempo por la muerte de jugadores. Desde el día 7, [`StormNaturalTask`](../../src/main/java/me/difficulty/features/storm/day7/StormNaturalTask.java) realiza una tirada **una vez por segundo**:

| Evento | Probabilidad / duración |
|---|---|
| Inicio natural (o ampliación si ya existe una Tormenta) | **1 entre 10.000 por segundo = 0,01%** |
| Duración elegida: opción corta | **30 minutos**, con **20%** |
| Duración elegida: opción larga | **1 hora**, con **80%** |

Las probabilidades de 30 minutos y una hora se evalúan **solamente cuando ocurre la tirada de Tormenta natural**. Si la Tormenta ya está activa, el tiempo obtenido se **suma**, no se reemplaza.

**Sistema general de muertes:** [`StormDeathListener`](../../src/main/java/me/difficulty/features/storm/core/listeners/StormDeathListener.java) añade `max(1, día actual)` horas al morir un jugador. Por ejemplo, una muerte en el día 10 añade 10 horas. [`StormManager`](../../src/main/java/me/difficulty/features/storm/core/StormManager.java) administra duración, estado, persistencia, clima y BossBar. Esta regla base no es una novedad exclusiva del día 7, pero resulta necesaria para entender la acumulación de tiempo.

## 4. Día 10: zombis personalizados

Desde el día 10, el sistema transforma zombis elegibles en variantes diferenciadas. La selección se organiza en [`ZombieSpawnListener`](../../src/main/java/me/difficulty/features/day10/mobs/zombies/ZombieSpawnListener.java).

| Mob | Salud | Equipamiento y atributos | Participación configurada |
|---|---|---|---|
| **Infrazombie** | La de un zombi normal | Resistencia al Fuego, **Velocidad V**, espada de madera con **Aspecto Ígneo V** | **40 partes** |
| **Zombie Corrupto** | **50 HP (25 corazones)** | **Lentitud I**, resistencia al empuje, armadura completa de netherita, espada de netherita con **Filo II** | **30 partes** |
| **Carroñero** | **10 HP (5 corazones)** | **Velocidad II**, hacha de diamante con **Filo XL (40)**, casco de hierro con **Protección II** | **25 partes** |

Las 40, 30 y 25 partes se eligen mediante una tirada de **95 resultados** para zombis elegibles que llegan a este listener. El otro **5%** corresponde al intento separado de reemplazo por el Zombie Gigante en generación natural con terreno válido. Por eso no debe interpretarse que, en cualquier condición y tipo de aparición, el mundo garantiza exactamente 5% gigantes, 40% infrazombis, 30% corruptos y 25% carroñeros.

### 4.1. Infrazombie

La intención es introducir presión por **velocidad y fuego**. Conserva la salud base de un zombi ordinario, pero se mueve mucho más rápido, no sufre el efecto habitual del fuego y porta un arma con Aspecto Ígneo V.

Clase principal: [`Infrazombie`](../../src/main/java/me/difficulty/features/day10/mobs/zombies/Infrazombie.java).

### 4.2. Zombie Corrupto

Es la variante de **resistencia y control**. Tiene 50 puntos de salud, armadura completa de netherita **sin encantamiento de Protección añadido por esta clase**, resistencia al empuje y una espada de Filo II. Su lentitud compensa parte de esa resistencia.

Su habilidad especial es un **aturdimiento de 3 segundos** al golpear al jugador. [`CorruptedZombieCombatListener`](../../src/main/java/me/difficulty/features/day10/mobs/zombies/CorruptedZombieCombatListener.java) almacena el fin del aturdimiento y restringe el movimiento del jugador durante ese tiempo.

Clases principales: [`CorruptedZombie`](../../src/main/java/me/difficulty/features/day10/mobs/zombies/CorruptedZombie.java) y `CorruptedZombieCombatListener`.

### 4.3. Carroñero

Es la variante de **alto daño y baja resistencia**: solo dispone de 10 HP, pero utiliza un hacha con Filo XL. Además, **el 40%** de los Carroñeros se convierte en zombi bebé, aumentando la dificultad de enfrentarlos de cerca.

Clase principal: [`Scavenger`](../../src/main/java/me/difficulty/features/day10/mobs/zombies/Scavenger.java).

## 5. Día 10: esqueletos personalizados

El sistema utiliza dos variantes principales. [`SkeletonSpawnListener`](../../src/main/java/me/difficulty/features/day10/mobs/skeletons/SkeletonSpawnListener.java) distribuye las apariciones elegibles entre **85% Esqueleto Elemental** y **15% Gladiador**.

### 5.1. Esqueleto Elemental

| Característica | Valor |
|---|---|
| Salud | **40 HP (20 corazones)** |
| Arma | Arco con **Fuego I** |
| Armadura | Hierro completo con **Protección II** |
| Disparo múltiple | **20%** de disparar **tres flechas** en lugar de una |
| Efectos al impactar | Cinco efectos posibles, detallados abajo |

**Efectos sobre jugadores:** cada uno tiene **5%** de probabilidad por la tirada de impacto, dura **10 segundos**, y las opciones son excluyentes en esa tirada:

| Efecto | Nivel | Probabilidad |
|---|---|---|
| Wither | II | 5% |
| Lentitud | II | 5% |
| Veneno | II | 5% |
| Ceguera | I | 5% |
| Debilidad | II | 5% |
| Ninguno de los anteriores | — | 75% |

La lógica se distribuye entre [`ElementalSkeleton`](../../src/main/java/me/difficulty/features/day10/mobs/skeletons/ElementalSkeleton.java), que configura el mob, y [`ElementalSkeletonCombatListener`](../../src/main/java/me/difficulty/features/day10/mobs/skeletons/ElementalSkeletonCombatListener.java), que administra las flechas y sus efectos.

### 5.2. Gladiador

| Característica | Valor |
|---|---|
| Salud | **80 HP (40 corazones)** |
| Movimiento | **Lentitud I** |
| Arco | **Poder LXX (70)** y **Retroceso II** (encantamiento `PUNCH`) |
| Espada | Netherita con **Filo L (50)** y **Empuje V** |
| Armadura | Diamante completo con **Protección II** |

El Gladiador representa una amenaza tanto a distancia como cuerpo a cuerpo. La clase [`GladiatorWeaponTask`](../../src/main/java/me/difficulty/features/day10/mobs/skeletons/GladiatorWeaponTask.java) gestiona su elección de arma según la cercanía del objetivo, en lugar de dejarlo con una sola modalidad fija.

Clases principales: [`Gladiator`](../../src/main/java/me/difficulty/features/day10/mobs/skeletons/Gladiator.java) y `GladiatorWeaponTask`.

## 6. Día 10: Creeper Futurista

El **Creeper Futurista** utiliza una explosión personalizada orientada a incrementar el peligro sin arruinar construcciones.

| Propiedad | Diseño implementado |
|---|---|
| Aparición entre creepers elegibles | **70%** Futurista, **30%** normal |
| Tiempo de preparación | `setMaxFuseTicks(15)`; menor que el comportamiento habitual |
| Radio configurado en el creeper | **5** |
| Alcance usado por el cálculo de daño a entidades | Hasta **10 bloques** (`radio × 2`) |
| Daño | Fórmula personalizada con multiplicador **×2** sobre la referencia utilizada |
| Paredes | **No reducen** el daño personalizado |
| Bloques | **No se destruyen** por este listener |

La lógica se separa en [`FuturisticCreeper`](../../src/main/java/me/difficulty/features/day10/mobs/creepers/FuturisticCreeper.java), [`CreeperSpawnListener`](../../src/main/java/me/difficulty/features/day10/mobs/creepers/CreeperSpawnListener.java) y [`FuturisticCreeperExplosionListener`](../../src/main/java/me/difficulty/features/day10/mobs/creepers/FuturisticCreeperExplosionListener.java).

**Decisión de desarrollo:** en vez de depender del daño vanilla, `FuturisticCreeperExplosionListener` calcula el daño sobre las entidades vivas cercanas y distingue ese daño del evento vanilla para evitar aplicarlo dos veces. Al mismo tiempo limpia la lista de bloques afectados y deja el rendimiento y el balance del radio bajo control del código.

La referencia matemática multiplica por dos el daño base calculado **a una posición relativa dentro del radio ampliado**. No debe interpretarse como una medición exacta de «dos veces lo que habría hecho cualquier creeper vanilla a la misma distancia absoluta», porque las áreas de alcance son distintas.

## 7. Día 10: arañas personalizadas

[`SpiderSpawnListener`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/SpiderSpawnListener.java) selecciona variantes para arañas normales elegibles. Las arañas de cueva no se convierten mediante ese listener.

| Variante | Probabilidad | Salud | Rasgo principal |
|---|---:|---:|---|
| **Araña Velocista** | **35%** | Vanilla | Velocidad IV, invisibilidad y escalada mejorada |
| **Araña del Abismo** | **20%** | **70 HP** | Fuerza VIII y aparición de enemigos al morir |
| **Tejedora** | **35%** | Vanilla | Posibilidad de portar un esqueleto jinete |
| **Goliat** | **10%** | **160 HP** | Fuerza III, Resistencia I, Velocidad I, efectos y telarañas |

### 7.1. Araña Velocista

- **Velocidad IV** permanente e **invisibilidad**.
- **50%** de posibilidades de obtener `Glowing`, lo que puede revelar su contorno.
- La clase `SpeedSpiderClimbTask` aumenta su velocidad vertical mientras está trepando; el valor mínimo aplicado a la componente vertical es **0,40** en las condiciones configuradas.

Clases: [`SpeedSpider`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/SpeedSpider.java) y [`SpeedSpiderClimbTask`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/SpeedSpiderClimbTask.java).

### 7.2. Araña del Abismo

- **70 HP (35 corazones)**, **Fuerza VIII** y **Lentitud I**.
- Al morir: **90%** de generar **tres arañas de cueva**, o **10%** de generar **una Goliat**.
- Ambas posibilidades son ramas de **una misma tirada**, no dos eventos independientes.

Clases: [`AbyssSpider`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/AbyssSpider.java) y [`AbyssSpiderDeathListener`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/AbyssSpiderDeathListener.java).

### 7.3. Tejedora

- **80%** de probabilidad de crear un esqueleto montado.
- El jinete puede ser un **Esqueleto Elemental (85%)** o un **Gladiador (15%)**, según la selección implementada en `Weaver`.
- Los proyectiles del jinete tienen **30%** de probabilidad de colocar una telaraña al impactar al jugador y **10%** de aplicar **Fatiga Minera I durante 60 segundos**.
- El jinete se identifica mediante una marca persistente adicional; así los efectos especiales no se aplican a todos los esqueletos del servidor.

Clases: [`Weaver`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/Weaver.java) y [`WeaverRiderCombatListener`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/WeaverRiderCombatListener.java).

### 7.4. Goliat

- **160 HP (80 corazones)**.
- Efectos permanentes: **Fuerza III**, **Resistencia I** y **Velocidad I**.
- Al impactar al jugador, **20%** de probabilidad de aplicar simultáneamente **Náusea V** y **Veneno V** durante **10 segundos**.
- **10%** de probabilidad de colocar una telaraña en la posición del jugador al golpear.
- Al morir, añade **2–5 ojos de araña fermentados** y **2–4 telarañas**.

Clases: [`Goliath`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/Goliath.java) y [`GoliathListener`](../../src/main/java/me/difficulty/features/day10/mobs/spiders/GoliathListener.java).

## 8. Día 10: golems, caballos y mascotas

Esta etapa no se limita a criaturas hostiles. También adapta entidades normalmente neutrales o pacíficas para cambiar las prioridades de los jugadores al explorar, domesticar animales y combatir.

### 8.1. Golem de Hierro

| Cambio | Valor |
|---|---|
| Velocidad | **Velocidad III** |
| Efecto del golpe | **Lentitud I durante 30 segundos** |
| Recompensa extra | **70%** de añadir **1 bloque de hierro** al morir |

Implementación: [`IronGolemListener`](../../src/main/java/me/difficulty/features/day10/mobs/golems/IronGolemListener.java).

### 8.2. Caballos

| Condición | Resultado |
|---|---|
| Jugador montado en el caballo | **Regeneración II** para el jugador |
| Aparición de un caballo | **5%** de obtener **Velocidad III** |
| Caballo con armadura equipada | **Resistencia II** para el caballo |

Implementación: [`HorseSpawnListener`](../../src/main/java/me/difficulty/features/day10/mobs/caballos/HorseSpawnListener.java) y [`HorseEffectTask`](../../src/main/java/me/difficulty/features/day10/mobs/caballos/HorseEffectTask.java). La tarea periódica permite mantener los efectos mientras se cumplan las condiciones de uso o equipamiento.

### 8.3. Gatos y lobos

- Una vez **domesticados**, reciben **Fuerza II** mediante la lógica periódica del plugin.
- Los ejemplares **salvajes** pueden comportarse agresivamente frente a jugadores y causar **Debilidad IV durante 10 segundos**.
- Los **gatos** cuentan con lógica de ataque contra creepers y se protege a los gatos frente al daño de explosiones.
- Los **lobos** que lleven armadura de armadillo (`WOLF_ARMOR`) reciben **Resistencia I**.

Implementación: [`PetBehaviorTask`](../../src/main/java/me/difficulty/features/pets/PetBehaviorTask.java), [`PetCombatListener`](../../src/main/java/me/difficulty/features/pets/PetCombatListener.java) y `WolfControlListener`.

## 9. Nuevos materiales y Ballesta Explosiva

### 9.1. Materiales personalizados

| Material | Fuente | Probabilidad | Cantidad |
|---|---|---:|---:|
| **Tinta Corrupta** | Muerte de un **calamar brillante** | **80%** | **2–4** |
| **Cuerno de Minotauro** | Muerte de un **Ravager** | **100%** en la implementación actual | **1** |

Estos drops se habilitan desde el día 10. Los items son instancias personalizadas, no simples nombres cambiados de materiales vanilla: utilizan un `custom_item_id` persistente, gestionado por `CustomItem`.

Archivos: [`CorruptedInkDropListener`](../../src/main/java/me/difficulty/features/day10/items/corruptedink/CorruptedInkDropListener.java), [`MinotaurHornDropListener`](../../src/main/java/me/difficulty/features/day10/items/minotaurhorn/MinotaurHornDropListener.java), `CorruptedInk` y `MinotaurHorn`.

### 9.2. Ballesta Explosiva: diseño

La Ballesta Explosiva es un arma especial e **irrompible** destinada a proporcionar daño de área desde el día 10.

**Comportamiento implementado:**

1. El usuario dispara la Ballesta Explosiva con el tipo de munición que haya cargado.
2. `ExplosiveCrossbowShootListener` identifica el arma por su ID personalizado.
3. El proyectil original se sustituye por un **cohete personalizado** con efecto visual rojo.
4. Al explotar el cohete, el listener calcula **daño explosivo personalizado de potencia 3** sobre las entidades vivas cercanas.
5. El código distingue su propio daño del daño vanilla del cohete para **evitar duplicación**.
6. No se llama a `World.createExplosion()` para ese daño de área: **el sistema personalizado no destruye bloques ni objetos tirados**.
7. La implementación también intenta restaurar las velocidades originales de las entidades para neutralizar el empuje.

| Parámetro | Configuración |
|---|---|
| Potencia de daño configurada | `EXPLOSION_POWER = 3F` |
| Radio de búsqueda del daño | `3 × 2 = 6 bloques` |
| Potencia de vuelo visual del cohete | `FIREWORK_POWER = 1` (no define el daño) |
| Daño bloqueado por paredes | **No**: se decidió **conservar el daño a través de obstáculos** |
| Rotura de bloques / destrucción de loot por explosión de mundo | **No** en este sistema personalizado |
| Durabilidad | **Irrompible** |
| Duplicación de daño vanilla | Cancelación específica para el cohete custom |

> **Decisión de balance de Beta 9:** se consideró reducir el daño al existir paredes, pero se **descartó** esa modificación. La Ballesta conserva el daño a través de obstáculos; se evaluará su balance con las futuras mecánicas de los días 17–20. No debe documentarse `rayTraceBlocks()` como parte de la versión final actual de este arma.

La potencia 3 es un parámetro de **esta implementación personalizada**; no equivale a invocar una explosión vanilla de mundo de fuerza 3.

### 9.3. Receta de la Ballesta Explosiva

| | Columna 1 | Columna 2 | Columna 3 |
|---|---|---|---|
| **Fila 1** | 5 Tintas Corruptas | 20 Cuernos de Minotauro | 5 Tintas Corruptas |
| **Fila 2** | 2 cabezas de Wither Skeleton | 1 ballesta vanilla | 2 cabezas de Wither Skeleton |
| **Fila 3** | 5 Tintas Corruptas | 20 Cuernos de Minotauro | 5 Tintas Corruptas |

**Totales por fabricación:** 20 Tintas Corruptas, 40 Cuernos de Minotauro, 4 cabezas de Wither Skeleton y 1 ballesta vanilla.

La receta se declara en [`ExplosiveCrossbowRecipe`](../../src/main/java/me/difficulty/features/day10/items/explosivecrossbow/ExplosiveCrossbowRecipe.java). [`ExplosiveCrossbowRecipeListener`](../../src/main/java/me/difficulty/features/day10/items/explosivecrossbow/ExplosiveCrossbowRecipeListener.java) valida los tipos reales, los identificadores personalizados y la **cantidad por casilla**, para evitar que la receta sea consumida como una fabricación vanilla de una unidad por posición. También existe `ExplosiveCrossbowRecipeBookListener` para la integración con el libro de recetas.

Para acelerar las pruebas de desarrollo se incorporó `/giveexplosivecrossbow`. Este comando utiliza directamente `ExplosiveCrossbow.createItem()`, por lo que entrega **el objeto reconocido por el plugin**, no una ballesta vanilla renombrada.

## 10. La Tormenta a partir del día 10

La Tormenta es un sistema que comienza a existir antes de esta etapa y que, desde el día 10, añade reglas capaces de alterar combates, aparición de enemigos y recursos.

### 10.1. Bonificaciones de criaturas

Durante una Tormenta activa y con día lógico **10 o superior**:

- Los **mobs hostiles, neutrales y pacíficos** reciben una mejora de **Fuerza I y Resistencia I**.
- Si ya tenían Fuerza o Resistencia, el sistema está diseñado para aumentar el nivel respecto a su efecto previo, **no simplemente sustituir siempre todo por nivel I**.
- Las bonificaciones se gestionan y revierten a través de [`StormMobEffectTask`](../../src/main/java/me/difficulty/features/storm/day10/tasks/StormMobEffectTask.java).

Los **jugadores** no reciben esta bonificación general como si fueran mobs. Sus posibles efectos negativos de Tormenta se gestionan separadamente.

### 10.2. Mob cap ×2 y apariciones adicionales

[`StormMobCapTask`](../../src/main/java/me/difficulty/features/storm/day10/tasks/StormMobCapTask.java) utiliza:

```java
world.setSpawnLimit(SpawnCategory.MONSTER, stormLimit);
```

Por decisión de diseño, **el multiplicador ×2 se limita a la categoría `MONSTER`**. No se extiende la mob cap de animales pacíficos: el objetivo es intensificar la amenaza, no compensarla con más criaturas pasivas.

[`StormExtraSpawnTask`](../../src/main/java/me/difficulty/features/storm/day10/tasks/StormExtraSpawnTask.java) añade un intento de aparición extra **por jugador** mientras se cumplen las condiciones de Tormenta:

| Restricción / parámetro | Valor implementado |
|---|---|
| Tirada de intento por jugador | **20%** en cada ejecución (tarea programada cada segundo) |
| Jugadores excluidos | Creativo, espectador y jugadores muertos |
| Distancia objetivo de aparición | Aproximadamente **24–40 bloques** |
| Límite local usado por el sistema | **30 enemigos hostiles** dentro del radio configurado |
| Límite de mobs extra identificados por mundo | **60** |
| Posiciones candidatas | Se someten a comprobaciones de espacio, superficie, seguridad y chunks cargados |

El 20% corresponde a un **intento**, no a la garantía de que aparezca un mob. Cuando no se encuentra ubicación adecuada —por ejemplo, en una cueva estrecha— el intento **se descarta**, en vez de forzar un enemigo junto al jugador. Esta decisión es deliberada para mantener la dificultad sin crear apariciones injustas.

### 10.3. Reglas especiales

| Evento | Comportamiento |
|---|---|
| **Evoker derrotado** | Se eliminan los **Tótems de la Inmortalidad** de sus drops durante la Tormenta. |
| **Ender Pearl utilizada** | **10%** de probabilidad de cancelar el teletransporte asociado; la perla ya se lanzó. |
| **Mob custom elegible derrotado** | **50%** de probabilidad de añadir **2–7 diamantes**. |

La recompensa adicional de diamantes se aplica a los mobs personalizados identificados: Zombie Gigante, Infrazombie, Zombie Corrupto, Carroñero, Esqueleto Elemental, Gladiador, Creeper Futurista, Araña Velocista, Araña del Abismo, Tejedora y Goliat. El listener comprueba la identidad guardada en el mob; **no se trata de un drop general para todos los enemigos vanilla**.

Archivos: `StormEvokerDropListener`, [`StormEnderPearlListener`](../../src/main/java/me/difficulty/features/storm/day10/listeners/StormEnderPearlListener.java) y [`StormDiamondDropListener`](../../src/main/java/me/difficulty/features/storm/day10/listeners/StormDiamondDropListener.java).

### 10.4. Efectos sobre jugadores

Cada **10 segundos**, cada jugador conectado participa en una tirada independiente mientras están activos los efectos de Tormenta del día 10.

| Resultado | Nivel | Duración | Probabilidad por tirada |
|---|---|---|---:|
| Wither | I | 10 s | 1% |
| Lentitud | I | 10 s | 1% |
| Ceguera | I | 10 s | 1% |
| Debilidad | I | 10 s | 1% |
| Veneno | I | 10 s | 1% |
| Hambre | I | 10 s | 1% |
| Ningún efecto | — | — | **94%** |

Son resultados de **una sola tirada**, no seis pruebas independientes por jugador. La implementación se encuentra en [`StormPlayerEffectTask`](../../src/main/java/me/difficulty/features/storm/day10/tasks/StormPlayerEffectTask.java), programada cada **200 ticks (10 segundos)** desde `StormBootstrap`.

### 10.5. Persistencia y compatibilidad con el sistema general

`StormManager` conserva su estado en la configuración del plugin: actividad, tiempo restante, duración acumulada y día en que comenzó. Administra también la BossBar y la continuidad del evento. El disparo natural de día 7 y las muertes de jugadores pueden **acumular tiempo sobre la misma Tormenta**.

Se incluye una precaución adicional para creepers durante la Tormenta: `CreeperPotionCloudListener` elimina efectos activos del creeper al iniciar la explosión para evitar nubes de pociones no deseadas asociadas a esa mecánica.

## 11. Estructura de desarrollo y arquitectura Java

El proyecto mantiene una separación explícita entre **núcleo reutilizable**, **funciones del juego**, **listeners/tareas** y **registro de componentes**.

### 11.1. Árbol de paquetes relevante

```text
src/main/java/me/difficulty/
├── DifficultyPlugin.java                # Punto de entrada de Paper
├── bootstrap/
│   ├── CoreBootstrap.java               # Sistemas básicos y registros
│   ├── Day7Bootstrap.java               # Día 7: gigante y estrella
│   ├── Day10Bootstrap.java              # Día 10: coordinación principal
│   ├── day10/
│   │   ├── Day10ItemRegistrar.java       # Instancias de objetos
│   │   ├── Day10MobRegistrar.java        # Instancias de mobs
│   │   ├── Day10ItemListenerRegistrar.java
│   │   ├── Day10ZombieSkeletonListenerRegistrar.java
│   │   ├── Day10CreeperListenerRegistrar.java
│   │   ├── Day10SpiderListenerRegistrar.java
│   │   ├── Day10OtherCreatureListenerRegistrar.java
│   │   ├── Day10TaskRegistrar.java
│   │   └── Day10CommandRegistrar.java
│   └── storm/StormBootstrap.java         # Tormenta: listeners y tasks
├── core/
│   ├── day/DayManager.java               # Día lógico y persistencia
│   ├── items/                             # CustomItem y registro
│   ├── mobs/                              # CustomMob, IDs y registro
│   └── recipes/                           # Desbloqueo de recetas
├── features/
│   ├── day7/
│   │   ├── gigante/                       # IA, combate, spawn, drops
│   │   └── corruptedstar/                 # Objeto, receta, cooldown
│   ├── day10/
│   │   ├── items/                         # Materiales y ballesta
│   │   └── mobs/                          # Zombis, esqueletos, etc.
│   ├── storm/
│   │   ├── core/                          # Duración, BossBar, muertes
│   │   ├── day7/                          # Activación natural
│   │   └── day10/                         # Nuevos eventos y efectos
│   ├── pets/                              # Gatos y lobos
│   └── qualityoflife/                     # Mejoras independientes
└── commands/                             # Comandos generales
```

### 11.2. Arranque del plugin

[`DifficultyPlugin.onEnable()`](../../src/main/java/me/difficulty/DifficultyPlugin.java) coordina las etapas de registro en un orden que importa:

1. Carga o crea la configuración.
2. Inicializa `DayManager`, `StormBootstrap` y los registros generales.
3. Registra objetos del día 7 y día 10, así como las clases de mobs.
4. Crea y registra recetas.
5. Inicializa el desbloqueo de recetas por día.
6. Registra listeners de combate, aparición, drops, objetos y Tormenta.
7. Inicia las tareas periódicas de IA, mascotas y Tormenta.
8. Registra comandos administrativos y de prueba.
9. Actualiza las recetas descubiertas de los jugadores conectados.

Esto evita depender de instancias que todavía no han sido creadas y mantiene el punto de entrada como **orquestador**, no como contenedor de toda la lógica del juego.

### 11.3. Identidad persistente de mobs y objetos

Un zombi personalizado sigue siendo una entidad de tipo `Zombie`; distinguirlo exige algo más que comprobar su nombre. La clase base [`CustomMob`](../../src/main/java/me/difficulty/core/mobs/CustomMob.java) almacena `custom_mob_id` con `PersistentDataContainer` al aplicar una variante. Los listeners consultan ese ID mediante `CustomMobRegistry` para afectar únicamente al mob correcto.

De forma análoga, [`CustomItem`](../../src/main/java/me/difficulty/core/items/CustomItem.java) genera el ítem con:

- Material base de Minecraft.
- Nombre y lore personalizados.
- Identificador persistente `custom_item_id`.
- Valor `CustomModelData` reservado para una apariencia personalizada.

Esto evita, por ejemplo, que una ballesta vanilla renombrada sea reconocida como Ballesta Explosiva. **`CustomModelData` no instala ni genera por sí mismo las texturas**; requerirá un resource pack compatible cuando llegue la fase visual.

### 11.4. Diferencia entre listener, task y Bootstrap

| Componente | Responsabilidad | Ejemplo |
|---|---|---|
| **Modelo / configuración del mob** | Determinar vida, efectos y equipamiento inicial | `Scavenger`, `Gladiator`, `Goliath` |
| **Listener** | Reaccionar a un evento de Paper | `GiantDropListener`, `StormEnderPearlListener` |
| **Task** | Actualizar periódicamente una mecánica | `GiantAiTask`, `StormExtraSpawnTask` |
| **Registry** | Mantener y consultar instancias o identidades | `CustomMobRegistry`, `Day10MobRegistrar` |
| **Bootstrap** | Inicializar, conectar y registrar sistemas | `Day7Bootstrap`, `Day10Bootstrap`, `StormBootstrap` |
| **Comando** | Herramienta de administración o prueba | `GiveExplosiveCrossbowCommand` |

Esta organización se eligió para que futuras etapas puedan añadir comportamientos sin reescribir el punto de entrada ni confundir el código de distintas familias de criaturas.

### 11.5. Configuración y recetas

- [`config.yml`](../../src/main/resources/config.yml): día lógico y estado persistido de la Tormenta.
- [`plugin.yml`](../../src/main/resources/plugin.yml): nombre del plugin, clase principal, versión de API, comandos y permisos.
- [`RecipeUnlockManager`](../../src/main/java/me/difficulty/core/recipes/RecipeUnlockManager.java): revela o retira recetas conocidas según si el día permite utilizarlas.
- `CorruptedStarRecipeListener` y `ExplosiveCrossbowRecipeListener`: verifican cantidades exigidas por casilla y evitan que el crafteo de objetos especiales se trate como un consumo vanilla estándar.

## 12. Decisiones técnicas y evolución del balance

El resultado documentado es **Beta 9**. Se ha contrastado el TXT de balance con la implementación final de `main`; no es una transcripción sin comprobaciones. Los principales criterios que guiaron la etapa son:

| Tema | Decisión adoptada | Motivo técnico / de juego |
|---|---|---|
| **Gigante** | IA propia y aparición validada en superficie | Evitar spawns inadecuados y permitir persecución/combate reales. |
| **Zombie Corrupto** | 50 HP y armadura de netherita sin Protección adicional | Mantener resistencia sin multiplicar de forma innecesaria los encantamientos. |
| **Carroñero** | 10 HP y **Filo XL** | Enemigo frágil, pero especialmente peligroso si logra conectar golpes. |
| **Gladiador** | **Poder LXX** y **Filo L** | Dar relevancia a combate a distancia y cuerpo a cuerpo. |
| **Creeper Futurista** | Explosión personalizada, radio mayor y daño a través de paredes | Aumentar el riesgo sin destruir construcciones. |
| **Ballesta Explosiva** | Potencia 3, conservación de objetos y **sin reducción por paredes** | Mantenerla útil en esta etapa; su equilibrio se reevaluará al introducir futuras mecánicas. |
| **Tormenta: mob cap** | ×2 **solo para `MONSTER`** | No aumentar también las apariciones pacíficas. |
| **Tormenta: spawns adicionales** | Descartar intentos sin posición segura | Evitar enemigos colocados artificialmente junto a jugadores en cuevas. |
| **Ender Pearl** | **10% de fallo** | Igualar el valor definitivo de Beta 9. |
| **Comandos de prueba** | Entrega directa de la Ballesta | Reducir el tiempo perdido en crafteos repetidos durante la depuración. |

### Aspectos que conviene distinguir

- **Revisión de código frente a prueba en Minecraft:** confirmar que una clase contiene un valor correcto no garantiza por sí solo el resultado en todas las condiciones del servidor.
- **Probabilidad configurada frente a probabilidad efectiva:** las restricciones de superficie, tipo de spawn, espacio y límites locales pueden reducir el número real de apariciones.
- **Comentarios antiguos:** algunos comentarios históricos de clases pueden conservar porcentajes anteriores, aunque las condiciones ejecutables ya reflejen Beta 9. La documentación debe seguir **el código vigente**, no esos comentarios obsoletos.
- **Cambios posteriores:** cuando se diseñen los días 17–20, no se alterará retroactivamente la descripción de Beta 9 sin anotar expresamente qué fue modificado.

## 13. Comandos, pruebas y criterios de aceptación

### 13.1. Comandos disponibles

| Comando | Uso en esta etapa |
|---|---|
| `/day` | Consultar el día lógico. |
| `/setday 7` | Probar Gigante, Estrella y Tormenta natural. |
| `/setday 10` | Probar todo el contenido de día 10. |
| `/givecorruptedstar` | Recibir la Estrella Corrupta. |
| `/spawngiant` | Probar IA, persecución y ataque del Gigante. |
| `/giveexplosivecrossbow` | Recibir directamente la Ballesta Explosiva. |
| `/spawninfrazombie` / `/spawncorruptedzombie` / `/spawnscavenger` | Probar las variantes de zombi. |
| `/spawnelementalskeleton` / `/spawngladiator` | Probar los esqueletos. |
| `/spawnfuturisticcreeper` | Probar el Creeper Futurista. |
| `/spawnspeedspider` / `/spawnabyssspider` / `/spawnweaver` / `/spawngoliath` | Probar las arañas. |
| `/storm start` / `/storm stop` | Activar o detener la Tormenta para verificar mecánicas especiales. |

Los comandos administrativos requieren los permisos declarados en `plugin.yml`, normalmente disponibles a operadores. El comando de entrega de la Ballesta **no elimina su restricción de uso antes del día 10**.

### 13.2. Compilación y entorno

Requisitos: **Paper 1.21.5** y **Java 21**. Construcción con Gradle Wrapper desde la raíz del repositorio:

```powershell
.\gradlew.bat clean build
```

En Linux/macOS:

```bash
./gradlew clean build
```

Instalar el JAR obtenido en `build/libs/` dentro de `plugins/` y reiniciar Paper antes de repetir las pruebas. Una compilación satisfactoria y la ausencia de errores en la consola son pasos básicos, **no una validación automática de todas las mecánicas**.

### 13.3. Lista de pruebas recomendadas

Esta lista constituye un **plan de verificación reproducible**. No se marca como automáticamente ejecutado o aprobado por el simple hecho de estar descrito.

- [ ] **Día 7:** confirmar que el Gigante aparece solo en superficie válida y no dentro de cuevas.
- [ ] **Gigante:** comprobar exploración, persecución, distancia de golpe, cooldown y drops de 4–8 con la probabilidad esperada.
- [ ] **Día 10:** confirmar que el Gigante ya no recibe los fragmentos de netherita añadidos por su listener.
- [ ] **Estrella Corrupta:** validar Velocidad III durante 30 s, Debilidad V ocasional y cooldown de 60 s, incluida reconexión.
- [ ] **Receta de la Estrella:** comprobar cantidades por casilla y consumo correcto con clic normal y combinaciones de fabricación múltiple.
- [ ] **Zombis:** validar sus atributos, aturdimiento de 3 s, casco/armadura, daño de Filo XL y 40% de Carroñeros bebé.
- [ ] **Esqueletos:** comprobar efectos de flechas, tiro triple, encantamientos y cambio de armas del Gladiador.
- [ ] **Creeper Futurista:** confirmar radio ampliado, daño detrás de una pared, ausencia de daño duplicado y ausencia de rotura de bloques.
- [ ] **Arañas:** comprobar velocidad de escalada, invisibilidad/Glowing, aparición al morir, jinetes, telarañas y drops de Goliat.
- [ ] **Otras criaturas:** comprobar efectos de golems, caballos, gatos y lobos, incluso al cambiar equipamiento o domesticación.
- [ ] **Materiales:** confirmar Tinta Corrupta (80%, 2–4) y Cuerno de Minotauro (1).
- [ ] **Receta de la Ballesta:** validar cantidades por casilla, naturaleza del ítem resultante e irrompibilidad.
- [ ] **Ballesta:** confirmar potencia 3, daño real sobre mobs, daño único por impacto y comportamiento elegido detrás de obstáculos.
- [ ] **Objetos tirados:** lanzar objetos al suelo y matar mobs con la Ballesta; confirmar que el efecto personalizado no elimina el loot.
- [ ] **Tormenta natural:** verificar acumulación de duración, distribución de 30 min/1 h y persistencia tras reinicio.
- [ ] **Tormenta día 10:** validar mob cap de categoría `MONSTER`, buffs, intentos seguros de spawn extra y restauración al terminar.
- [ ] **Tormenta y jugadores:** comprobar el 10% de fallo de Ender Pearls y los seis efectos de 1% cada 10 segundos.
- [ ] **Tormenta y drops:** comprobar Evokers sin tótems y diamantes 2–7 al 50% para mobs custom elegibles.
- [ ] **Regresión:** comprobar que mobs y armas vanilla no reciben por error el comportamiento especial de las variantes personalizadas.

**Para probabilidades**, utilizar suficientes repeticiones y registrar los resultados; unos pocos intentos no permiten concluir que el porcentaje exacto esté equivocado.

## 14. Estado de cierre y próximos pasos

### Estado de esta etapa

- El documento de balance **Beta 9** define el contenido de referencia de los días 7–10.
- Los cambios de balance, Ballesta, Creeper, criaturas y Tormenta se integraron en `main` mediante el commit [`5ee3d16`](https://github.com/Ferch0o/Proyecto_0/commit/5ee3d16eadeeb2999a54cd299126b8f6cf14e6e9).
- Se verificó en el código integrado la probabilidad de **10%** de fallo de Ender Pearl, el valor de **potencia 3** de la Ballesta y el límite `MONSTER` ×2, entre otras reglas.
- Las verificaciones de integración y lectura de código **no deben interpretarse como una suite automática completa de pruebas del servidor**. Los criterios de aceptación anteriores deben servir para futuras regresiones.

### Próxima etapa: días 17–20

El siguiente trabajo será definir la nueva dificultad en su propio documento de balance, crear una rama desde `main` y añadir sistemas nuevos mediante el mismo patrón de **modelo + listener/task + registro + Bootstrap**.

La Ballesta Explosiva **permanece sin reducción de daño por paredes en Beta 9**. Se podrá reevaluar posteriormente cuando estén definidos los cambios de la siguiente etapa, sin anticipar aquí un nerfeo no implementado.

La futura etapa dispondrá de documentación independiente en `docs/dia-17-20/README.md` cuando esté desarrollada y verificada. Se repetirá el mismo procedimiento para días 25–30 y día 35.

---

**Referencias de este README:** especificación `Dia 7 - 10 Version_Beta(9).txt`, código de [`main`](https://github.com/Ferch0o/Proyecto_0/tree/main) en el commit de integración indicado y las decisiones finales de balance del desarrollo de esta etapa. Este README describe el estado de esa versión; no promete automáticamente compatibilidad con futuros cambios de Minecraft o Paper.
