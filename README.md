# Selenium

[![CI](https://github.com/SeleniumHQ/selenium/actions/workflows/ci.yml/badge.svg?branch=trunk&event=schedule)](https://github.com/SeleniumHQ/selenium/actions/workflows/ci.yml)

<a href="https://selenium.dev"><img src="https://selenium.dev/images/selenium_logo_square_green.png" width="180" alt="Selenium"/></a>

Selenium is an umbrella project encapsulating a variety of tools and
libraries enabling web browser automation. Selenium specifically
provides an infrastructure for the [W3C WebDriver specification](https://w3c.github.io/webdriver/)
— a platform and language-neutral coding interface compatible with all
major web browsers.

The project is made possible by volunteer contributors who've
generously donated thousands of hours in code development and upkeep.

Selenium's source code is made available under the [Apache 2.0 license](https://github.com/SeleniumHQ/selenium/blob/trunk/LICENSE).

This README is for developers interested in contributing to the project.
For people looking to get started using Selenium, please check out
our [User Manual](https://selenium.dev/documentation/) for detailed examples and descriptions, and if you
get stuck, there are several ways to [Get Help](https://www.selenium.dev/support/).

## Contributing

Please read [CONTRIBUTING.md](https://github.com/SeleniumHQ/selenium/blob/trunk/CONTRIBUTING.md)
before submitting your pull requests.


## Installing

These are the requirements to cereate your own local dev environment to contribute to Selenium.

### All Platforms
* [Bazelisk](https://github.com/bazelbuild/bazelisk), a Bazel wrapper that automatically downloads
  the version of Bazel specified in `.bazelversion` file and transparently passes through all
  command-line arguments to the real Bazel binary.
* Java JDK version 17 or greater (e.g., [Java 17 Temurin](https://adoptium.net/temurin/releases/?version=17))
  * Set `JAVA_HOME` environment variable to location of Java executable (the JDK not the JRE)
  * To test this, try running the command `javac`. This command won't exist if you only have the JRE
  installed. If you're met with a list of command-line options, you're referencing the JDK properly.

### MacOS
  * Xcode including the command-line tools. Install the latest version using: `xcode-select --install`
  * Rosetta for Apple Silicon Macs. Add `build --host_platform=//:rosetta` to the `.bazelrc.local` file. We are working
  to make sure this isn't required in the long run.

### Windows
Several years ago [Jim Evans](https://www.linkedin.com/in/jimevansmusic/) published a great article on
[Setting Up a Windows Development Environment for the Selenium .NET Language Bindings](https://jimevansmusic.blogspot.com/2020/04/setting-up-windows-development.html);
This article is out of date, but it includes more detailed descriptions and screenshots that some people might find useful.

<details>
<summary>Click to see Current Windows Setup Requirements</summary>

#### Option 1: Automatic Installation from Scratch
This script will ensure a complete ready to execute developer environment. 
(nothing is installed or set that is already present unless otherwise prompted) 

1. Open Powershell as an Administrator
2. Execute: `Set-ExecutionPolicy Bypass -Scope Process -Force` to allow running the script in the process
3. Navigate to the directory you want to clone Selenium in, or the parent directory of an already cloned Selenium repo 
4. Download and execute this script in the powershell terminal: [scripts/dev-environment-setup.ps1]`

#### Option 2: Manual Installation
1. Allow running scripts in Selenium in general:
    ```
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned
    ```
2. Enable Developer Mode:
    ```
    reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\AppModelUnlock" /t REG_DWORD /f /v "AllowDevelopmentWithoutDevLicense" /d "1"
    ```
3. Install [MSYS2](https://www.msys2.org/), which is an alternative shell environment that provides Unix-like commands
    * Add bin directory to `PATH` environment variable (e.g., `"C:\tools\msys64\usr\bin"`)
    * Add `bash.exe` location as the `BAZEL_SH` environment variable (e.g., `"C:\tools\msys64\usr\bin\bash.exe"`)
4. Install the latest version of [Visual Studio Community](https://visualstudio.microsoft.com/vs/community/)
    * Use the visual studio installer to modify and add the "Desktop development with C++" Workload
    * Add Visual C++ build tools installation directory location to `BAZEL_VC` environment variable (e.g. `"C:\Program Files\Microsoft Visual Studio\2022\Community\VC"`)
    * Add Visual C++ Build tools version to `BAZEL_VC_FULL_VERSION` environment variable (this can be discovered from the directory name in `"$BAZEL_VC\Tools\MSVC\<BAZEL_VC_FULL_VERSION>"`)
5. Add support for long file names (bazel has a lot of nested directories that can exceed default limits in Windows)
    * Enable Long Paths support with these 2 registry commands:
    ```shell
    reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Command Processor" /t REG_DWORD /f /v "DisableUNCCheck" /d "1"
    reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\FileSystem" /t REG_DWORD /f /v "LongPathsEnabled" /d "1"
    ```
    * Allow Bazel to create short name versions of long file paths: `fsutil 8dot3name set 0`
    * Set bazel output to `C:/tmp` instead of nested inside project directory:
        * Create a file `selenium/.bazelrc.windows.local`
        * Add "startup --output_user_root=C:/tmp" to the file

</details>

### Alternative Dev Environments

If you want to contribute to the project, but do not want to set up your own local dev environment,
there are two alternatives available.

#### Using GitPod

Rather than creating your own local dev environment, GitPod provides a ready to use environment for you.

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/SeleniumHQ/selenium)

#### Using Docker Image

You can also build a Docker image suitable
for building and testing Selenium using the Dockerfile in the
[dev image](scripts/dev-image/Dockerfile) directory.


## Building

Selenium is built using a common build tool called [Bazel](https://bazel.build/), to 
allow us to easily manage dependency downloads, generate required binaries, build and release packages, and execute tests;
all in a fast, efficient manner. For a more detailed discussion, read Simon Stewart's article on [Building Selenium](https://www.selenium.dev/blog/2023/building-selenium/)

Often we wrap Bazel commands with our custom [Rake](http://rake.rubyforge.org/) wrapper. These are run with the `./go` command.

The common Bazel commands are:
* `bazel build` — evaluates dependencies, compiles source files and generates output files for the specified target.
It's used to create executable binaries, libraries, or other artifacts.
* `bazel run` — builds the target and then executes it. 
It's typically used for targets that produce executable binaries.
* `bazel test` — builds and runs the target in a context with additional testing functionality
* `bazel query` — identifies available targets for the provided path.

Each module that can be built is defined in a `BUILD.bazel` file. To execute the module you refer to it starting with a 
`//`, then include the relative path to the file that defines it, then `:`, then the name of the target.
For example, the target to build the Grid is named `executable-grid` and it is
defined in the `'selenium/java/src/org/openqa/selenium/grid/BAZEL.build'` file.
So to build the grid you would run: `bazel build //java/src/org/openqa/selenium/grid:executable-grid`.

The Bazel documentation has a [handy guide](https://bazel.build/run/build#specifying-build-targets)
for various shortcuts and all the ways to build multiple targets, which Selenium makes frequent use of.

To build everything for a given language:
```shell
bazel build //<language>/...
```

To build just the grid there is an alias name to use (the log will show where the output jar is located):
```sh
bazel build grid
```

To make things more simple, building each of the bindings is available with this `./go` command
```shell
./go <language>:build
```


## Developing

### Java

Most of the team uses Intellij for their day-to-day editing. If you're
working in IntelliJ, then we highly recommend installing the [Bazel IJ
plugin](https://plugins.jetbrains.com/plugin/8609-bazel) which is documented on
[its own site](https://plugins.jetbrains.com/plugin/8609-bazel).

To use Selenium with the IntelliJ Bazel plugin, import the repository as a Bazel project, and select the project
view file from the [scripts](scripts) directory. `ij.bazelproject` for Mac/Linux and `ij-win.bazelproject` for Windows.

We also use Google Java Format for linting, so using the Google Java Formatter Plugin is useful;
there are a few steps to get it working, so read their [configuration documentation](https://github.com/google/google-java-format/blob/master/README.md#intellij-jre-config)

To install Selenium locally based on a specific commit, you can use:
```shell
./go java:install
```

### Python

You can run Python code locally by updating generated files in the python directory using:
```shell
./go py:update
```

To install Selenium locally based on a specific commit, you can use:
```shell
./go py:install
```

### Ruby

Instead of using `irb`, you can create an interactive REPL with all gems loaded using: `bazel run //rb:console`

If you want to debug code, you can do it via [`debug`](https://github.com/ruby/debug) gem:
1. Add `binding.break` to the code where you want the debugger to start.
2. Run tests with  `ruby_debug` configuration: `bazel test --config ruby_debug <test>`.
3. When debugger starts, run the following in a separate terminal to connect to debugger:

```sh
bazel-selenium/external/bundle/bin/rdbg -A
```

If you want to use [RubyMine](https://www.jetbrains.com/ruby/) for development, 
you can configure it use Bazel artifacts:

1. Open `rb/` as a main project directory.
2. Run `bundle exec rake update` as necessary to create up-to-date artifacts. If this does not work, run `./go rb:update` from the `selenium` (parent) directory.
3. In <kbd>Settings / Languages & Frameworks / Ruby SDK and Gems</kbd> add new <kbd>Interpreter</kbd> pointing to `../bazel-selenium/external/rules_ruby_dist/dist/bin/ruby`.
4. You should now be able to run and debug any spec. It uses Chrome by default, but you can alter it using environment variables secified in [Ruby Testing](#ruby-2) section below.

### Rust

To keep `Carbo.Bazel.lock` synchronized with `Cargo.lock`, run:
```shell
CARGO_BAZEL_REPIN=true bazel sync --only=crates
```

### Tour of Repo

The codebase is generally segmented around the languages used to
write the component. Selenium makes extensive use of JavaScript, so
let's start there. First of all, start the development server:

```sh
bazel run debug-server
```

Now, navigate to
[http://localhost:2310/javascript](http://localhost:2310/javascript).
You'll find the contents of the `javascript/` directory being shown.
We use the [Closure Library](https://developers.google.com/closure/library/)
for developing much of the JavaScript, so now navigate to
[http://localhost:2310/javascript/atoms/test](http://localhost:2310/javascript/atoms/test).

The tests in this directory are normal HTML files with names ending
with `_test.html`.  Click on one to load the page and run the test.

### Maven _per se_

Selenium is not built with Maven. It is built with `bazel`,
though that is invoked with `go` as outlined above,
so you do not have to learn too much about that.

That said, it is possible to relatively quickly build Selenium pieces
for Maven to use. You are only really going to want to do this when
you are testing the cutting-edge of Selenium development (which we
welcome) against your application. Here is the quickest way to build
and deploy into your local maven repository (`~/.m2/repository`), while
skipping Selenium's own tests.

```sh
./go maven-install
```

The maven jars should now be in your local `~/.m2/repository`.

### Updating Java dependencies

The coordinates (_groupId_:_artifactId_:_version_) of the Java dependencies
are defined in the file [maven_deps.bzl](https://github.com/SeleniumHQ/selenium/blob/trunk/java/maven_deps.bzl).
The process to modify these dependencies is the following:

1. (Optional) If we want to detect the dependencies which are not updated,
   we can use the following command for automatic discovery:

    ```sh
    bazel run @maven//:outdated
    ```

2. Modify [maven_deps.bzl](https://github.com/SeleniumHQ/selenium/blob/trunk/java/maven_deps.bzl).
   For instance, we can bump the version of a given artifact detected in the step before.

3. Repin dependencies. This process is required to update the file [maven_install.json](https://github.com/SeleniumHQ/selenium/blob/trunk/java/maven_install.json),
   which is used to manage the Maven dependencies tree (see [rules_jvm_external](https://github.com/bazelbuild/rules_jvm_external) for further details). The command to carry out this step is the following:

    ```sh
    RULES_JVM_EXTERNAL_REPIN=1 bazel run @unpinned_maven//:pin
    ```

4. (Optional) If we use IntelliJ with the Bazel plugin, we need to synchronize
   our project. To that aim, we click on _Bazel_ &rarr; _Sync_ &rarr; _Sync Project
   with BUILD Files_.


## Testing

There are a number of bazel configurations specific for testing.

### Common Options Examples

Here are examples of arguments we make use of in testing the Selenium code:
* `--pin_browsers=true` - run specific browser versions defined in the build (versions are updated regularly)
* `--flaky_test_attempts 3` - re-run failed tests up to 3 times
* `--local_test_jobs 1` - control parallelism of tests
* `--no-cache_test_results`, `-t-` - disable caching of test results and re-runs all of them
* `--test_output all` - print all output from the tests, not just errors
* `--test_output streamed` - run all tests one by one and print its output immediately
* `--test_env FOO=bar` - pass extra environment variable to test process
* `--run_under="xvfb-run -a"` - prefix to insert before the execution

### Filtering

Selenium tests can be filtered by size:
* small — typically unit tests where no browser is opened
* large — typically tests that actually drive a browser
* medium — tests that are more involved than simple unit tests, but not fully driving a browser

These can be filtered using the `test_size_filters` argument like this:
```sh
bazel test //<language>/... --test_size_filters=small
```

Tests can also be filtered by tag like:
```sh
bazel test //<language>/... --test_tag_filters=this,-not-this
```

### Java

<details>
<summary>Click to see Java Test Commands</summary>

To run unit tests:
```shell
bazel test //java/... --test_size_filters=small
```
To run integration tests:
```shell
bazel test //java/... --test_size_filters=medium
```
To run browser tests:
```shell
bazel test //java/... --test_size_filters=large --test_tag_filters=<browser>
```

To run a specific test:
```shell
bazel test //java/test/org/openqa/selenium/chrome:ChromeDriverFunctionalTest
```

</details>

### JavaScript
<details>
<summary>Click to see JavaScript Test Commands</summary>

To run the tests run:

```sh
bazel test //javascript/node/selenium-webdriver:tests
```

You can use `--test_env` to pass in the browser name as `SELENIUM_BROWSER`.

```sh
bazel test //javascript/node/selenium-webdriver:tests --test_env=SELENIUM_BROWSER=firefox
```

</details>

### Python
<details>
<summary>Click to see Python Test Commands</summary>

Run unit tests with:
```shell
bazel test //py:unit
```

To run tests with a specific browser:

```sh
bazel test //py:test-<browsername>
```

To run all Python tests:
```shell
bazel test //py:all
```

</details>

### Ruby
<details>
<summary>Click to see Ruby Test Commands</summary>

Test targets:

| Command                                                                              | Description                                    |
|--------------------------------------------------------------------------------------|------------------------------------------------|
| `bazel test //rb/...`                                                                | Run unit, integration tests (Chrome) and lint  |
| `bazel test //rb:lint`                                                               | Run RuboCop linter                             |
| `bazel test //rb/spec/...`                                                           | Run unit and integration tests (Chrome)        |
| `bazel test --test_size_filters large //rb/...`                                      | Run integration tests using (Chrome)           |
| `bazel test //rb/spec/integration/...`                                               | Run integration tests using (Chrome)           |
| `bazel test //rb/spec/integration/... --define browser=firefox`                      | Run integration tests using (Firefox)          |
| `bazel test //rb/spec/integration/... --define remote=true`                          | Run integration tests using (Chrome and Grid)  |
| `bazel test //rb/spec/integration/... --define browser=firefox --define remote=true` | Run integration tests using (Firefox and Grid) |
| `bazel test --test_size_filters small //rb/...`                                      | Run unit tests                                 |
| `bazel test //rb/spec/unit/...`                                                      | Run unit tests                                 |

Ruby test modules have the same name as the spec file with `_spec.rb` removed, so you can run them individually:

| Test file                                                      | Test target                                              |
|----------------------------------------------------------------|----------------------------------------------------------|
| `rb/spec/integration/selenium/webdriver/chrome/driver_spec.rb` | `//rb/spec/integration/selenium/webdriver/chrome:driver` |
| `rb/spec/unit/selenium/webdriver/proxy_spec.rb`                | `//rb/spec/unit/selenium/webdriver:proxy`                |

Supported browsers:

* `chrome`
* `edge`
* `firefox`
* `ie`
* `safari` (cannot be run in parallel - use `--local_test_jobs 1`)
* `safari-preview` (cannot be run in parallel - use `--local_test_jobs 1`)

Useful command line options:

In addition to the [Common Options Examples](#common-options-examples), here are some additional Ruby specific ones:
* `--test_arg "-tfocus"` - test only [focused specs](https://relishapp.com/rspec/rspec-core/v/3-12/docs/filtering/inclusion-filters)
* `--test_arg "-eTimeouts"` - test only specs which name include "Timeouts"
* `--test_arg "<any other RSpec argument>"` - pass any extra RSpec arguments (see `bazel run @bundle//:bin/rspec -- --help`)

Supported environment variables for use with `--test_env`:

- `WD_SPEC_DRIVER` - the driver to test; either the browser name or 'remote' (gets set by Bazel)
- `WD_REMOTE_BROWSER` - when `WD_SPEC_DRIVER` is `remote`; the name of the browser to test (gets set by Bazel)
- `WD_REMOTE_URL` - URL of an already running server to use for remote tests
- `DOWNLOAD_SERVER` - when `WD_REMOTE_URL` not set; whether to download and use most recently released server version for remote tests
- `DEBUG` - turns on verbose debugging
- `HEADLESS` - for chrome, edge and firefox; runs tests in headless mode
- `DISABLE_BUILD_CHECK` - for chrome and edge; whether to ignore driver and browser version mismatches (allows testing Canary builds)
- `CHROME_BINARY` - path to test specific Chrome browser
- `EDGE_BINARY` - path to test specific Edge browser
- `FIREFOX_BINARY` - path to test specific Firefox browser

To run with a specific version of Ruby you can change the version in `rb/.ruby-version` or from command line:

```sh
echo '<X.Y.Z>' > rb/.ruby-version
```
</details>

### .NET
<details>
<summary>Click to see .NET Test Commands</summary>

.NET tests currently only work with pinned browsers, so make sure to include that.

Run all tests with:
```sh
bazel test //dotnet/test/common:AllTests --pin_browsers=true
```

You can run specific tests by specifying the class name:
```shell
bazel test //dotnet/test/common/ElementFindingTest --pin_browsers=true
```

If the module supports multiple browsers:
```shell
bazel test //dotnet/test/common/ElementFindingTest-edge --pin_browsers=true
```

</details>

### Rust
<details>
<summary>Click to see Rust Test Commands</summary>

Rust tests are run with:

```shell
bazel test //rust/...
```
</details>

### Linux

<details>
<summary>Click to see Linux Testing Requirements</summary>

By default, Bazel runs these tests in your current X-server UI. If you prefer, you can
alternatively run them in a virtual or nested X-server.

1. Run the X server `Xvfb :99` or `Xnest :99`
2. Run a window manager, for example, `DISPLAY=:99 jwm`
3. Run the tests you are interested in:
```sh
bazel test --test_env=DISPLAY=:99 //java/... --test_tag_filters=chrome
```

An easy way to run tests in a virtual X-server is to use Bazel's `--run_under`
functionality:
```
bazel test --run_under="xvfb-run -a" //java/...
```
</details>


## Documenting

API documentation can be found here:

* [C#](https://seleniumhq.github.io/selenium/docs/api/dotnet/)
* [JavaScript](https://seleniumhq.github.io/selenium/docs/api/javascript/)
* [Java](https://seleniumhq.github.io/selenium/docs/api/java/index.html)
* [Python](https://seleniumhq.github.io/selenium/docs/api/py/)
* [Ruby](https://seleniumhq.github.io/selenium/docs/api/rb/)

To update API documentation for a specific language: `./generate_api_docs.sh <language>`

To update all documentation: `./generate_api_docs.sh all`

Note that JavaScript generation is [currently broken](https://github.com/SeleniumHQ/selenium/issues/10185).


## Releasing

The full process for doing a release can be found in [the wiki](https://github.com/SeleniumHQ/selenium/wiki/Releasing-Selenium)

Releasing is a combination of building and publishing, which often requires coordination of multiple executions
and additional processing. 
As discussed in the [Building](#building) section, we use Rake tasks with the `./go` command for these things.
These `./go` commands include the `--stamp` argument to provide necessary information about the constructed asset.

You can build and release everything with:
```shell
./go all:release
```

To build and release a specific language:
```shell
./go <language>:release
```

If you have access to the Selenium EngFlow repository, you can have the assets built remotely and downloaded locally using:
```shell
./go all:release['--config', 'release']
```
