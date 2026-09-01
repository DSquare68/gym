# Gym Tracker

Track workouts, personal records and progress charts.

A [HUB](https://github.com/homeforge/hub) plugin. Its build config —
dependencies, versions, jar/shade setup — is inherited from
[`homeforge-plugin-starter-parent`](https://github.com/DSquare68/homeforge-plugin-template),
so `pom.xml` here only declares this plugin's identity, path and schema.

## Plugin identity

| Property | Value |
|----------|-------|
| Path     | `/gym` |
| Schema   | `gym_schema` |
| Id       | `gym` |

These live in `pom.xml` (`plugin.path` / `plugin.schema`) and
`com.github.dsquare68.gym.PluginInfo` - keep both in sync if you change them later.

## Project layout

```
gym/
├── pom.xml                                 <- identity + opt-in persistence deps (versions in parent)
└── src/main/
    ├── java/.../com.github.dsquare68.gym/
    │   ├── PluginBootstrap.java             <- PF4J entry point
    │   ├── HubPluginImpl.java               <- HUB lifecycle (path, schema, routes)
    │   ├── PluginInfo.java                  <- plugin identity constants
    │   ├── entity/
    │   │   ├── ExerciseName.java            <- catalogue row (@Entity)
    │   │   ├── TrainingRecord.java          <- a scheme, or a session performed against one
    │   │   ├── TrainingExercise.java
    │   │   ├── TrainingRound.java           <- one set
    │   │   └── PerformedSet.java            <- read projection: a set with its session around it
    │   ├── repository/
    │   │   ├── ExerciseNameRepository.java  <- extends JpaRepository<ExerciseName, Long>
    │   │   └── TrainingRepository.java      <- extends JpaRepository<TrainingRecord, Long>
    │   ├── persistence/
    │   │   ├── GymPersistenceConfig.java    <- EntityManagerFactory + tx manager + repositories
    │   │   └── GymPersistence.java          <- owns the JPA context for the plugin's lifetime
    │   ├── seed/
    │   │   └── ExerciseSeed.java            <- reads the seed file
    │   └── view/
    │       ├── MainView.java                <- served at plugin.path
    │       └── DashboardWidget.java         <- dashboard card
    └── resources/
        ├── db/seed/
        │   └── exercises_merged_final_1.0.json   <- base exercise set
        └── META-INF/
            └── extensions.idx                <- PF4J extension index
```

## Database

Persistence is Spring Data JPA over Hibernate. HUB plugins get no Spring
container of their own — the SPI is deliberately Spring-free — so the plugin
stands up a small `ApplicationContext` internally, in
[`GymPersistenceConfig`](src/main/java/com/github/dsquare68/gym/persistence/GymPersistenceConfig.java),
around the `DataSource` from `db()`:

```java
GymPersistence persistence = new GymPersistence(db().dataSource());
persistence.trainings().history(userId, "Bench Press");
```

`db()` is the pool on the PostgreSQL role HUB provisioned for this plugin. That
role owns `gym_schema` and holds no grants anywhere else, so nothing the JPA
context does can land outside this plugin's own schema.

`HubPluginImpl` builds the context on activation and closes it on deactivation.

### Dependencies and classloading

Versions for the whole generic stack — Spring, the JPA/Hibernate libraries,
Lombok — are pinned in `homeforge-plugin-starter-parent`. This `pom.xml` only
lists the artifacts it actually opts into (no versions) plus the one
gym-specific library, `jackson-databind`, for the seed file.

Spring Framework core (`spring-context`, `spring-beans`, …) is inherited from the
parent as **`provided`** — the HUB host ships it, exactly as it ships `spring-web`.
Spring Data JPA and Hibernate are **not** on the host, so this plugin declares
them (`compile`) and the shade plugin bundles them into the jar (which is why it
is ~33 MB). The two sets load from different classloaders, but there is only one
copy of each type, so nothing clashes across the PF4J boundary. If a future HUB
host stops shipping `spring-context` transitively, flip the five Spring-core
`<scope>provided</scope>` entries in the **parent** pom to `compile`.

### Schema management

Hibernate owns the schema (`hibernate.hbm2ddl.auto=update`): the tables are
derived from the `@Entity` classes the first time the context starts, and a jar
that adds a mapped column or entity extends the schema on next activation. The
tradeoff: `update` adds tables and columns but never rewrites or drops one. The
day a change has to backfill existing rows, add Flyway against the same
`db().dataSource()` and switch `hbm2ddl.auto` to `validate` — nothing here
forecloses that.

## Seeding the exercise catalogue

`db/seed/exercises_merged_final_1.0.json` ships the base exercise set. It is
loaded on install and on every activation; the insert skips names that already
exist, so a shipped seed update reaches existing installs and exercises the user
added are never touched.

Treat it as an initial dataset, not a hard dependency — the catalogue is an
ordinary table that users keep adding to.

## Build your UI

`MainView` is the Vaadin view served at your `plugin.path`. Add sub-views and
return their routes from `HubPluginImpl#routes()`.

## Use platform APIs

Inside any lifecycle callback or view you can call HUB APIs through the
`HubApi` instance stored during `onActivate`:

```java
// Who is logged in?
HubUser me = api.user().currentUser();

// Send a notification
api.notifications().notifyUser(me.id(), "Hello!", Severity.INFO);

// Persist an entity
api.storage().save(new ExampleItem(...));
```

This plugin's own relational data does *not* go through `api.storage()` — that
runs with HUB's credentials. Anything touching `gym_schema` goes through the JPA
repositories, which sit on `db()`, this plugin's own role:

```java
persistence.exerciseNames().findAllByOrderByCategoryAscNameAsc();
persistence.trainings().history(userId, "Bench Press");
```

Neither repository knows about Vaadin or `HubApi` — every method takes the owning
user id as a parameter. That is the seam for the external API a phone would post
sessions to: it goes through the same repositories, not a second write path.

## Build & install

```bash
mvn clean package
# -> target/gym-0.0.1-SNAPSHOT.jar

# In HUB: Settings -> Plugins -> Upload Plugin -> choose the jar -> Install
```
