# Gym Tracker

${pluginDescription}

A [HUB](https://github.com/homeforge/hub) plugin generated from the
`homeforge-plugin-archetype`.

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
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/.../com.github.dsquare68.gym/
    │   ├── PluginBootstrap.java             <- PF4J entry point
    │   ├── HubPluginImpl.java               <- HUB lifecycle (path, schema, routes)
    │   ├── PluginInfo.java                  <- plugin identity constants
    │   ├── view/
    │   │   ├── MainView.java                <- served at plugin.path
    │   │   └── DashboardWidget.java         <- dashboard card
    │   ├── service/                         <- your business logic
    │   └── entity/                          <- your JPA entities / records
    └── resources/
        ├── db/migration/
        │   └── V1__init.sql                 <- Flyway migration
        └── META-INF/
            └── extensions.idx                <- PF4J extension index
```

## Write your database migrations

Add SQL files to `src/main/resources/db/migration/`:

```
V1__init.sql          <- already provided, edit the example table
V2__add_column.sql
...
```

Flyway runs them in order on first install, scoped to your schema.

## Build your UI

`MainView` is the Vaadin view served at your `plugin.path`. Add sub-views and
register their routes in `HubPluginImpl#registerRoutes`.

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

## Build & install

```bash
mvn clean package
# -> target/gym-0.0.1-SNAPSHOT.jar

# In HUB: Settings -> Plugins -> Upload Plugin -> choose the jar -> Install
```
