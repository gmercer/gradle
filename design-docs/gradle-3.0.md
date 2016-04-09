# Gradle 3.0

Gradle 3.0 is the next major Gradle release that offers the opportunity to make breaking changes to the public interface of Gradle. This document captures a laundry
list of ideas to consider before shipping Gradle 3.0.

Note: for the change listed below, the old behaviour or feature to be removed should be deprecated in a Gradle 2.x release. Similarly for changes to behaviour.

# Candidates for Gradle 3.0

The following stories are candidates to be included in a major release of Gradle. Currently, they are *not* scheduled to be included in Gradle 3.0.

## Change minimum version for running Gradle to Java 7

No longer support running Gradle, the wrapper or the Tooling api client on Java 6. Instead, we'd support Java source compilation and test execution on Java 6 and later, as we do for Java 1.5 now.

- Update project target versions, remove customisations for IDEA project generation.
- Remove java 7 checks, eg from continuous build.

### Test coverage

- Warning when running Gradle entry point on Java 6:
    - `gradle`
    - `gradle --daemon`
    - `gradlew`
    - `GradleConnector`
    - `GradleRunner`
    - old `gradlew`
    - old `GradleConnector`
- Warning when running build on Java 6 with entry point running on Java 7+ 
    - `gradle`
    - `gradle --daemon`
    - `gradlew`
    - `GradleConnector`
    - `GradleRunner`
    - old `gradlew`
    - old `GradleConnector`
    - old `GradleRunner`
- Can cross-compile and test for Java 6.

## Change minimum version for building and testing Java source to Java 6

Change cross-compilation and test execution to require Java 6 or later.
Building against Java 5 requires that the compiler daemon and test execution infrastructure still support Java 5.

- Clean up `DefaultClassLoaderFactory`.
- Change `InetAddressFactory` so that it no longer uses reflection to inspect `NetworkInterface`.
- Replace usages of `guava-jdk5`.

### Test coverage

- Warning when running tests on Java 5.

## Drop support for old versions of things

- Using TestNG Javadoc annotations. TestNG dropped support for this in 5.12, in early 2010. Supporting these old style annotations means we need to attach the test source files as an input to the `Test` task, which means there's an up-to-date check cost for this.
- Tooling api clients older than 2.0 (1.5 years old). Gradle 2.x supports tapi clients 1.2 and later. People using tooling older than 2.0 would have to upgrade to a newer version.
- Tooling api client running builds for Gradle versions older than 1.0 (3.75 years old). Tapi 2.x supports Gradle 1.0-m8 and later.
- Wrapper support for versions older than 2.0. Wrapper 2.x supports Gradle 0.9.2 and later (5 years) and Gradle 2.x can be run by wrapper 0.9.2 and later.
- Cached artefact reuse for versions older than 2.0.
- Execution of task classes compiled against Gradle versions older than 2.0.

## Remove Sonar plugins

Deprecate the Sonar plugins

## Test output directories

The current defaults for the outputs of tasks of type `Test` conflict with each other:

* Change the default result and report directory for the `Test` type to include the task's name, so that the default
  does not conflict with the default for any other `Test` task.
* Change the default TestNG output directory.

## Log changes

* Remove `--no-color` command-line option.
* Remove `LoggingConfiguration.colorOutput` property.
* Remove `LoggingManager.logLevel` property. Managing the log level should not be a concern of the build logic.
* Remove `Project.getLogging()` method. Would be replaced by the existing `logging` property on `Script` and `Task`.
* Move internal types `StandardOutputCapture`, `StandardOutputRedirector` and `LoggingManagerInternal` into an internal package.

## Archive tasks + base plugin

* Remove `org.gradle.api.tasks.bundling.Jar`, replaced by `org.gradle.jvm.tasks.Jar`.
* Move defaults for output directory and other attributes from the base plugin to an implicitly applied plugin, so that they are applied to all instances.
* Use `${task.name}.${task.extension}` as the default archive name, so that the default does not conflict with the default for any other archive task.

## Drop support for Gradle versions older than 1.0

* Cross version tests no longer test against anything earlier than 1.0
* Local artifact reuse no longer considers candidates from the artifact caches for Gradle versions earlier than 1.0
* Wrapper does not support downloading versions earlier than 1.0
* Remove old unused types that are baked into the bytecode of tasks compiled against older versions (eg `ConventionValue`). Fail with a reasonable
error message for these task types.

## Remove Ant <depend> based incremental compilation backend

Now we have real incremental Java compilation, remove the `CompileOptions.useDepend` property and related options.

## Remove the Gradle Open API stubs

* Remove the remaining Open API interfaces and stubs.
* Remove the `openApi` project.

## Remove `group` and `status` from project

Alternatively, default the group to `null` and status to `integration`.

## Remove the Ant-task based Scala compiler

* Change the default for `useAnt` to `false` and deprecate the `useAnt` property.
* Do a better job of matching up the target jvm version with the scala compiler backend.

## Don't inject tools.jar into the system ClassLoader

Currently required for in-process Ant-based compilation on Java 5. Dropping support for one of (in-process, ant-based, java 5) would allow us to remove this.

## Decouple publishing DSL from Maven classes

* Change the old publishing DSL to use the Maven 3 classes instead of Maven 2 classes. This affects:
    * `MavenResolver.settings`
    * `MavenDeployer.repository` and `snapshotRepository`.
    * `MavenPom.dependencies`.
* Remove `MavenDeployer.addProtocolProviderJars()`.
* Change `PublishFilter` so that it accepts a `PublishArtifact` instead of an `Artifact`.

## Copy tasks

There are several inconsistencies and confusing behaviours in the copy tasks and copy spec:

* Change copy tasks so that they no longer implement `CopySpec`. Instead, they should have a `content` property which is a `CopySpec` that contains the main content.
  Leave behind some methods which operate on the file tree as a whole, eg `eachFile()`, `duplicatesStrategy`, `matching()`.
* Change the copy tasks so that `into` always refers to the root of the destination file tree, and that `destinationDir` (possibly with a better name) is instead used
  to specify the root of the destination file tree, for those tasks that produce a file tree on the file system.
* Change the `Jar` type so that there is a single `metaInf` copy spec which is a child of the main content, rather than creating a new copy spec each time `metainf`
  is referenced. Do the same for `War.webInf`.
* The `CopySpec.with()` method currently assumes that a root copy spec is supplied with all values specified, and no values are inherited by the attached copy spec.
  Instead, change `CopySpec.with()` so that values are inherited from the copy spec.
    * Change `CopySpec` so that property queries do not query the parent value, as a copy spec may have multiple parents. Or, alternatively, allow only root copy spec
      to be attached to another using `with()`.
* Change the default duplicatesStrategy to `fail` or perhaps `warn`.
* Change the `Ear` type so that the generated descriptor takes precedence over a descriptor in the main content, similar to the manifest for `Jar` and the
  web XML for `War`.

## Remove old dependency result graph

The old dependency result graph is expensive in terms of heap usage. We should remove it.

* Promote new dependency result graph to un-incubate it.
* Remove methods that use `ResolvedDependency` and `UnresolvedDependency`.
* Keep `ResolvedArtifact` and replace it later, as it is not terribly expensive to keep.

## Remove API methods that are added by the DSL decoration

Some model types hand-code the DSL conventions in their API. We should remove these and let the DSL decoration take care of this, to simplify these
types and to offer a more consistent DSL.

* Remove all methods that accept a `Closure` when an `Action` overload is available. Add missing overloads where appropriate.
* Remove all methods that accept a `String` or `Object` when a enum overload is available. Add missing overloads where appropriate.
* Remove CharSequence -> Enum conversion code in `DefaultTaskLogging`.
* Remove all set methods that contain no custom logic.
* Formally document the Closure → Action coercion mechanism
    - Needs to be prominent enough that casual DSL ref readers understand this (perhaps such Action args are annotated in DSL ref)

## Tooling API clean ups

* `LongRunningOperation.withArguments()` should be called `setArguments()` for consistency.
* Remove support for consumers older than 1.6, will allow the provider to drop support for `BuildActionRunner` protocol.
* Remove the old `ProgressListener` interfaces and methods. These are superseded by the new interfaces. However, the new interfaces are supported only
  by Gradle 2.5 and later, so might need to defer the removal until 4.0.
* Move `UnsupportedBuildArgumentException` and `UnsupportedOperationConfigurationException` up to `org.gradle.tooling`, to remove
  package cycle from the API.

## Clean up `Task` DSL and hierarchy

* Remove the `<<` operator.
* Inline `ConventionTask` and `AbstractTask` into `DefaultTask`.
* Remove `Task.dependsOnTaskDidWork()`.
* Mix `TaskInternal` in during decoration and remove references to internal types from `DefaultTask` and `AbstractTask`

## Remove references to internal classes from API

* Remove `Configurable` from public API types.
* Remove `PomFilterContainer.getActivePomFilters()`.
* Change `StartParameter` so that it no longer extends `LoggingConfiguration`.
* Move `ConflictResolution` from public API (it's only used internally).
* Move `Module` from public API (it's only used internally).
* Move `Logging.ANT_IVY_2_SLF4J_LEVEL_MAPPER` from public API.
* Move `AntGroovydoc` and `AntScalaDoc` from public API.
* Move `BuildExceptionReporter`, `BuildResultLogger`, `TaskExecutionLogger` and `BuildLogger` out of the public API.

## Remove support for convention objects

Extension objects have been available for over 2 years and are now an established pattern.

* Migrate core plugins to use extensions.
* Remove `Convention` type.

## Project no longer inherits from its parent project

* Project should not delegate to its build script for missing properties or methods.
* Project should not delegate to its parent for missing properties or methods.
* Project build script classpath should not inherit anything from parent project.

## Container API tidy-ups

* Remove the specialised subclasses of `UnknownDomainObjectException` and the overridden methods that exist simply to declare this from `PluginContainer`, `ArtifactRepositoryContainer`,
  `ConfigurationContainer`, `TaskCollection`.
* Remove the specialised methods such as `whenTaskAdded()` from `PluginCollection`, `TaskCollection`
* Remove the `extends T` upper bound on the type variable of `DomainObjectCollection.withType()`.
* Remove the type variable from `ReportContainer`
* Move `ReportContainer.ImmutableViolationException` to make top level.

## Dependency API tidy-ups

* Remove `equals()` implementations from `Dependency` subclasses.
* Remove `ExternalDependency.force`. Use resolution strategy instead.
* Remove `SelfResolvingDependency.resolve()` methods. These should be internal and invoked only as part of resolution.
* Remove `ClientModule` and replace with consumer-side component meta-data rules.
* Remove `ExternalModuleDependency.changing`. Use component meta-data rules instead.

## Invocation API tidy-ups

* Remove the public `StartParameter` constructor.
* Remove the public `StartParameter` constants, `GRADLE_USER_HOME_PROPERTY_KEY` and `GRADLE_USER_HOME_PROPERTY_KEY`.
* Change `StartParameter` into an interface.

## Misc API tidy-ups

* Replace `TaskDependency.getDependencies(Task)` with `TaskDependency.getDependencies()`.
* Remove constants from `ExcludeRule`.
* Rename `IllegalDependencyNotation` to add `Exception` to the end of its name.
* Remove `ConventionProperty`, replace it with documentation.
* Remove `Settings.startParameter`. Can use `gradle.startParameter` instead.
* Remove `org.gradle.util` from default imports.
* Remove `AbstractOptions`.
* Remove unused `EclipseDomainModel`.
* Replace `ShowStacktrace.INTERNAL_EXCEPTIONS` with `NONE`.

## Signing plugin tidy-ups

- `SignatoryProvider` and sub-types should use container DSL instead of custom DSL.

## Decorate classes at load time instead of subclassing

Decorating classes at load time is generally a more reliable approach and offers a few new interesting use cases we can support. For example, by decorating classes
at load time we can support expressions such as `new MyDslType()`, rather than requiring that Gradle control the instantiation of decorated objects.

Switching to decoration at load time should generally be transparent to most things, except for clients of `ProjectBuilder` that refer to types
which are not loaded by Gradle, such as the classes under test.

## Restructure plugin package hierarchy

## buildNeeded and buildDependents

* Rename buildDependents to buildDownstream
* Rename buildNeeded to buildUpstream
* Add a new task buildStream which is equivalent to buildDownstream buildUpstream

## build.gradle in a multiproject build

* A Gradle best pattern is to name the gradle file to be the same name as the subproject.
* Let's support this out of the box, possibly as a preference to `build.gradle`, and maybe drop support for `build.gradle` in subprojects.
