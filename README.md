# Akka Http Spring Boot Starter
> akka-http-spring-boot-starter是spring boot与akka http集成的类库。
> akka-http-spring-boot-starter采用spring boot starter的方式，工程引起后，添加相关配置即可使用，akka-http-spring-boot-starter将根据配置自动完成启动和配置。
> akka-http-spring-boot-starter的目的是不使用servlet方式开发web项目，因此使用akka-http-spring-boot-starter后将不再需要tomcat这种web容器。
> 使用akka-http-spring-boot-starter集成Akka Http的工程将不会影响Spring的使用和Spring Boot对redis、mq等等的集成使用。
> 如果有兴趣的同学可以使用Akka中核心的Actor模型和Akka Stream进行开发。

## Maven
```
<dependency>
    <groupId>com.gomefinance.framework.http</groupId>
    <artifactId>akka-http-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```
## Akka
```
Akka is a toolkit and runtime for building highly concurrent,distributed,
and resilient message-driven applications on the JVM.---官方介绍
```
相关连接 ：
[官方网站](http://akka.io)
[分布式应用框架Akka快速入门](http://blog.csdn.net/jmppok/article/details/17264495)
[使用 Akka 执行异步操作](https://www.ibm.com/developerworks/cn/java/j-jvmc5/)
## Example
* 创建一个Maven工程，在pom中添加akka-http-spring-boot-starter引用。
* 创建一个spring boot Application的启动类。

```
@SpringBootApplication
public class TestApp {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(TestApp.class);
        app.run(args);
    }
}
```
* 创建MyRouteProccessor类并且实现RoutesProcessor抽象类。

```
/**
* Http Server路由的处理器，每个工程必须实现RoutesProcessor，否则 将无法使用。
*/
@Service
public class MyRouteProccessor extends RoutesProcessor {

    /**
     * 注入具体业务逻辑实现的Bean
     */
    @Autowired
    private MyProccssor myProccssor;

    /**
     * 组装业务处理Bean到处理器的槽中
     *
     */
    @Override
    public void assembleGroove() {
        /* key:Bean全局ID,bean: Bean的引用 */
        pull("b1", myProccssor);
    }

    /**
     * 自定义Route规则
     * @return
     */
    @Override
    public Route[] extensionRoutes() {
        return null;
    }
}
```
* 添加http server配置和路由配置

  yml方式:

```
  spring:
  akka:
    http:
      # 应用名称.
      application-name: paddington-test
      # 绑定的Host,可以为空,默认 0.0.0.0.
      bind-host: 0.0.0.0
      # 绑定的端口,可以为空,默认8080.
      port: 8080
      # contextPath路径,可以为空,默认为"",既root.
      contextPath: ""
      # 全局的请求入参类,可以为空.
      request-class: com.gomefinance.paddingtion.test.User
      # 全局的响应入参类,可以为空.
      response-class: com.gomefinance.paddingtion.test.Account
      # 路由规则定义,采用list的方式.
      routes:
        -
          # http方法
          httpMethod: POST
          # 绑定的URL
          url: account
          # url对于处理Bean的ID,既实现RouteProccessor抽象类的assembleGroove方法中pull的key.
          grooveId: b1
          # 实现url的Bean方法名称.
          method: registerAccount
          # URL对于逻辑实现方法的入参类,如果此处定义将覆盖全局的request-class.
          requestClass: com.gomefinance.paddingtion.test.User
          # URL对于逻辑实现方法的出参类,如果此处定义将覆盖全局的response-class.
          responseClass: com.gomefinance.paddingtion.test.Account
```
  如果定义多个route，见下面代码。

```
      routes:
        -
          # http方法
          httpMethod: POST
          # 绑定的URL
          url: account
          # url对于处理Bean的ID,既实现RouteProccessor抽象类的assembleGroove方法中pull的key.
          grooveId: b1
          # 实现url的Bean方法名称.
          method: registerAccount
          # URL对于逻辑实现方法的入参类,如果此处定义将覆盖全局的request-class.
          requestClass: com.gomefinance.paddingtion.test.User
          # URL对于逻辑实现方法的出参类,如果此处定义将覆盖全局的response-class.
          responseClass: com.gomefinance.paddingtion.test.Account
        -
          # http方法
          httpMethod: POST
          # 绑定的URL
          url: account
          # url对于处理Bean的ID,既实现RouteProccessor抽象类的assembleGroove方法中pull的key.
          grooveId: b1
          # 实现url的Bean方法名称.
          method: registerAccount
          # URL对于逻辑实现方法的入参类,如果此处定义将覆盖全局的request-class.
          requestClass: com.gomefinance.paddingtion.test.User
          # URL对于逻辑实现方法的出参类,如果此处定义将覆盖全局的response-class.
          responseClass: com.gomefinance.paddingtion.test.Account
```
* 增加akka配置文件application.conf,具体配置详情见官方网站文档。

```
akka {
  loglevel = INFO

  actor {
    provider = "local"

    guardian-supervisor-strategy = "akka.actor.DefaultSupervisorStrategy"

    default-dispatcher {
      # Must be one of the following
      # Dispatcher, PinnedDispatcher, or a FQCN to a class inheriting
      # MessageDispatcherConfigurator with a public constructor with
      # both com.typesafe.config.Config parameter and
      # akka.dispatch.DispatcherPrerequisites parameters.
      # PinnedDispatcher must be used together with executor=thread-pool-executor.
      type = "Dispatcher"

      # Which kind of ExecutorService to use for this dispatcher
      # Valid options:
      #  - "default-executor" requires a "default-executor" section
      #  - "fork-join-executor" requires a "fork-join-executor" section
      #  - "thread-pool-executor" requires a "thread-pool-executor" section
      #  - A FQCN of a class extending ExecutorServiceConfigurator
      executor = "default-executor"

      # This will be used if you have set "executor = "default-executor"".
      # If an ActorSystem is created with a given ExecutionContext, this
      # ExecutionContext will be used as the default executor for all
      # dispatchers in the ActorSystem configured with
      # executor = "default-executor". Note that "default-executor"
      # is the default value for executor, and therefore used if not
      # specified otherwise. If no ExecutionContext is given,
      # the executor configured in "fallback" will be used.
      default-executor {
        fallback = "fork-join-executor"
      }

      # This will be used if you have set "executor = "fork-join-executor""
      # Underlying thread pool implementation is akka.dispatch.forkjoin.ForkJoinPool
      fork-join-executor {
        # Min number of threads to cap factor-based parallelism number to
        parallelism-min = 32

        # The parallelism factor is used to determine thread pool size using the
        # following formula: ceil(available processors * factor). Resulting size
        # is then bounded by the parallelism-min and parallelism-max values.
        parallelism-factor = 3.0

        # Max number of threads to cap factor-based parallelism number to
        parallelism-max = 256

        # Setting to "FIFO" to use queue like peeking mode which "poll" or "LIFO" to use stack
        # like peeking mode which "pop".
        task-peeking-mode = "FIFO"
      }

      # This will be used if you have set "executor = "thread-pool-executor""
      # Underlying thread pool implementation is java.util.concurrent.ThreadPoolExecutor
      thread-pool-executor {
        # Keep alive time for threads
        keep-alive-time = 60s

        # Define a fixed thread pool size with this property. The corePoolSize
        # and the maximumPoolSize of the ThreadPoolExecutor will be set to this
        # value, if it is defined. Then the other pool-size properties will not
        # be used.
        #
        # Valid values are: `off` or a positive integer.
        fixed-pool-size = off

        # Min number of threads to cap factor-based corePoolSize number to
        core-pool-size-min = 8

        # The core-pool-size-factor is used to determine corePoolSize of the
        # ThreadPoolExecutor using the following formula:
        # ceil(available processors * factor).
        # Resulting size is then bounded by the core-pool-size-min and
        # core-pool-size-max values.
        core-pool-size-factor = 3.0

        # Max number of threads to cap factor-based corePoolSize number to
        core-pool-size-max = 256

        # Minimum number of threads to cap factor-based maximumPoolSize number to
        max-pool-size-min = 8

        # The max-pool-size-factor is used to determine maximumPoolSize of the
        # ThreadPoolExecutor using the following formula:
        # ceil(available processors * factor)
        # The maximumPoolSize will not be less than corePoolSize.
        # It is only used if using a bounded task queue.
        max-pool-size-factor  = 3.0

        # Max number of threads to cap factor-based maximumPoolSize number to
        max-pool-size-max = 256

        # Specifies the bounded capacity of the task queue (< 1 == unbounded)
        task-queue-size = -1

        # Specifies which type of task queue will be used, can be "array" or
        # "linked" (default)
        task-queue-type = "linked"

        # Allow core threads to time out
        allow-core-timeout = on
      }

      # How long time the dispatcher will wait for new actors until it shuts down
      shutdown-timeout = 1s

      # Throughput defines the number of messages that are processed in a batch
      # before the thread is returned to the pool. Set to 1 for as fair as possible.
      throughput = 5

      # Throughput deadline for Dispatcher, set to 0 or negative for no deadline
      throughput-deadline-time = 0ms

      # For BalancingDispatcher: If the balancing dispatcher should attempt to
      # schedule idle actors using the same dispatcher when a message comes in,
      # and the dispatchers ExecutorService is not fully busy already.
      attempt-teamwork = on

      # If this dispatcher requires a specific type of mailbox, specify the
      # fully-qualified class name here; the actually created mailbox will
      # be a subtype of this type. The empty string signifies no requirement.
      mailbox-requirement = ""
    }

    debug {
      autoreceive = on
    }
  }
}

akka.http {

  server {
    # The default value of the `Server` header to produce if no
    # explicit `Server`-header was included in a response.
    # If this value is the empty string and no header was included in
    # the request, no `Server` header will be rendered at all.
    server-header = akka-http/2.5.0

    # The time after which an idle connection will be automatically closed.
    # Set to `infinite` to completely disable idle connection timeouts.
    idle-timeout = 60 s

    # Defines the default time period within which the application has to
    # produce an HttpResponse for any given HttpRequest it received.
    # The timeout begins to run when the *end* of the request has been
    # received, so even potentially long uploads can have a short timeout.
    # Set to `infinite` to completely disable request timeout checking.
    #
    # If this setting is not `infinite` the HTTP server layer attaches a
    # `Timeout-Access` header to the request, which enables programmatic
    # customization of the timeout period and timeout response for each
    # request individually.
    request-timeout = 20 s

    # The time period within which the TCP binding process must be completed.
    bind-timeout = 1s

    # The time period the HTTP server implementation will keep a connection open after
    # all data has been delivered to the network layer. This setting is similar to the SO_LINGER socket option
    # but does not only include the OS-level socket but also covers the Akka IO / Akka Streams network stack.
    # The setting is an extra precaution that prevents clients from keeping open a connection that is
    # already considered completed from the server side.
    #
    # If the network level buffers (including the Akka Stream / Akka IO networking stack buffers)
    # contains more data than can be transferred to the client in the given time when the server-side considers
    # to be finished with this connection, the client may encounter a connection reset.
    #
    # Set to 'infinite' to disable automatic connection closure (which will risk to leak connections).
    linger-timeout = 1 min

    # The maximum number of concurrently accepted connections when using the
    # `Http().bindAndHandle` methods.
    #
    # This setting doesn't apply to the `Http().bind` method which will still
    # deliver an unlimited backpressured stream of incoming connections.
    #
    # Note, that this setting limits the number of the connections on a best-effort basis.
    # It does *not* strictly guarantee that the number of established TCP connections will never
    # exceed the limit (but it will be approximately correct) because connection termination happens
    # asynchronously. It also does *not* guarantee that the number of concurrently active handler
    # flow materializations will never exceed the limit for the reason that it is impossible to reliably
    # detect when a materialization has ended.
    max-connections = 24

    # The maximum number of requests that are accepted (and dispatched to
    # the application) on one single connection before the first request
    # has to be completed.
    # Incoming requests that would cause the pipelining limit to be exceeded
    # are not read from the connections socket so as to build up "back-pressure"
    # to the client via TCP flow control.
    # A setting of 1 disables HTTP pipelining, since only one request per
    # connection can be "open" (i.e. being processed by the application) at any
    # time. Set to higher values to enable HTTP pipelining.
    # This value must be > 0 and <= 1024.
    pipelining-limit = 12

    # Enables/disables the addition of a `Remote-Address` header
    # holding the clients (remote) IP address.
    remote-address-header = on

    # Enables/disables the addition of a `Raw-Request-URI` header holding the
    # original raw request URI as the client has sent it.
    raw-request-uri-header = off

    # Enables/disables automatic handling of HEAD requests.
    # If this setting is enabled the server dispatches HEAD requests as GET
    # requests to the application and automatically strips off all message
    # bodies from outgoing responses.
    # Note that, even when this setting is off the server will never send
    # out message bodies on responses to HEAD requests.
    transparent-head-requests = on

    # Enables/disables the returning of more detailed error messages to
    # the client in the error response.
    # Should be disabled for browser-facing APIs due to the risk of XSS attacks
    # and (probably) enabled for internal or non-browser APIs.
    # Note that akka-http will always produce log messages containing the full
    # error details.
    verbose-error-messages = off

    # The initial size of the buffer to render the response headers in.
    # Can be used for fine-tuning response rendering performance but probably
    # doesn't have to be fiddled with in most applications.
    response-header-size-hint = 512

    # The requested maximum length of the queue of incoming connections.
    # If the server is busy and the backlog is full the OS will start dropping
    # SYN-packets and connection attempts may fail. Note, that the backlog
    # size is usually only a maximum size hint for the OS and the OS can
    # restrict the number further based on global limits.
    backlog = 50

    # If this setting is empty the server only accepts requests that carry a
    # non-empty `Host` header. Otherwise it responds with `400 Bad Request`.
    # Set to a non-empty value to be used in lieu of a missing or empty `Host`
    # header to make the server accept such requests.
    # Note that the server will never accept HTTP/1.1 request without a `Host`
    # header, i.e. this setting only affects HTTP/1.1 requests with an empty
    # `Host` header as well as HTTP/1.0 requests.
    # Examples: `www.spray.io` or `example.com:8080`
    default-host-header = "paddington.receiver.gomefinance:18080"

    # Socket options to set for the listening socket. If a setting is left
    # undefined, it will use whatever the default on the system is.
    socket-options {
      so-receive-buffer-size = undefined
      so-send-buffer-size = undefined
      so-reuse-address = undefined
      so-traffic-class = undefined
      tcp-keep-alive = undefined
      tcp-oob-inline = undefined
      tcp-no-delay = undefined
    }

    # Modify to tweak parsing settings on the server-side only.
    parsing {
      # no overrides by default, see `akka.http.parsing` for default values
    }

    # Enables/disables the logging of unencrypted HTTP traffic to and from the HTTP
    # server for debugging reasons.
    #
    # Note: Use with care. Logging of unencrypted data traffic may expose secret data.
    #
    # Incoming and outgoing traffic will be logged in hexdump format. To enable logging,
    # specify the number of bytes to log per chunk of data (the actual chunking depends
    # on implementation details and networking conditions and should be treated as
    # arbitrary).
    #
    # For logging on the client side, see akka.http.client.log-unencrypted-network-bytes.
    #
    # `off` : no log messages are produced
    # Int   : determines how many bytes should be logged per data chunk
    log-unencrypted-network-bytes = off
  }

  stream {

    # Default flow materializer settings
    materializer {

      # Initial size of buffers used in stream elements
      initial-input-buffer-size = 4
      # Maximum size of buffers used in stream elements
      max-input-buffer-size = 16

      # Fully qualified config path which holds the dispatcher configuration
      # to be used by FlowMaterialiser when creating Actors.
      # When this value is left empty, the default-dispatcher will be used.
      dispatcher = ""

      # Cleanup leaked publishers and subscribers when they are not used within a given
      # deadline
      subscription-timeout {
        # when the subscription timeout is reached one of the following strategies on
        # the "stale" publisher:
        # cancel - cancel it (via `onError` or subscribing to the publisher and
        #          `cancel()`ing the subscription right away
        # warn   - log a warning statement about the stale element (then drop the
        #          reference to it)
        # noop   - do nothing (not recommended)
        mode = cancel

        # time after which a subscriber / publisher is considered stale and eligible
        # for cancelation (see `akka.stream.subscription-timeout.mode`)
        timeout = 5s
      }

      # Enable additional troubleshooting logging at DEBUG log level
      debug-logging = off

      # Maximum number of elements emitted in batch if downstream signals large demand
      output-burst-limit = 1000

      # Enable automatic fusing of all graphs that are run. For short-lived streams
      # this may cause an initial runtime overhead, but most of the time fusing is
      # desirable since it reduces the number of Actors that are created.
      # Deprecated, since Akka 2.5.0, setting does not have any effect.
      auto-fusing = on

      # Those stream elements which have explicit buffers (like mapAsync, mapAsyncUnordered,
      # buffer, flatMapMerge, Source.actorRef, Source.queue, etc.) will preallocate a fixed
      # buffer upon stream materialization if the requested buffer size is less than this
      # configuration parameter. The default is very high because failing early is better
      # than failing under load.
      #
      # Buffers sized larger than this will dynamically grow/shrink and consume more memory
      # per element than the fixed size buffers.
      max-fixed-buffer-size = 1000000000

      # Maximum number of sync messages that actor can process for stream to substream communication.
      # Parameter allows to interrupt synchronous processing to get upsteam/downstream messages.
      # Allows to accelerate message processing that happening withing same actor but keep system responsive.
      sync-processing-limit = 1000

      debug {
        # Enables the fuzzing mode which increases the chance of race conditions
        # by aggressively reordering events and making certain operations more
        # concurrent than usual.
        # This setting is for testing purposes, NEVER enable this in a production
        # environment!
        # To get the best results, try combining this setting with a throughput
        # of 1 on the corresponding dispatchers.
        fuzzing-mode = off
      }
    }

    # Fully qualified config path which holds the dispatcher configuration
    # to be used by FlowMaterialiser when creating Actors for IO operations,
    # such as FileSource, FileSink and others.
    blocking-io-dispatcher = "akka.stream.default-blocking-io-dispatcher"

    default-blocking-io-dispatcher {
      type = "Dispatcher"
      executor = "thread-pool-executor"
      throughput = 1

      thread-pool-executor {
        fixed-pool-size = 16
      }
    }
  }
}
```
* 扩展route，1.0版本只支持post方法的配置实现，如需get等方法的配置可以实现RoutesProcessor抽象类中的extensionRoutes方法。后续将不断完善Route定义配置这块的功能，实现最大数route配置化。

```
@Override
    public Route[] extensionRoutes() {

        Route[] routes = new Route[1];
        routes[0] = post(() ->
                path("account1" , () ->
                        entity(Jackson.unmarshaller(User.class), request -> {
                            CompletionStage<?> futureProcess
                                    = this.runSink(myProccssor,"registerAccount",new Class<?>[]{
                                User.class},request);

                            return onSuccess(() -> futureProcess, res ->
                                    completeOK(res, Jackson.marshaller())
                            );
                        })));
        return routes;
    }
```

相关官方文档：
[Akka Http Routes](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/routes.html)
[Akka Http Directives](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/index.html)

* 实现"/account"对于业务逻辑。

```
@Service
public class MyProccssor {


    public Account registerAccount(User user) {

        Account account = new Account();
        account.setAccount(user.getName() + "|" + user.getAge());

        return account;
    }
}
```

Account.class

```
public class Account {

    private String account;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
```

User.class

```
public class User {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```
* 启动工程，如果看到下面输出，恭喜你配置成功。

```
               _        _             \\  //(o)__(o)(o)__(o) ))          oo_     wWw ()_()wWw    wWw wWw ()_()
    /)  (OO) .' )(OO) .' )   /)       (o)(o)(__  __)(__  __)(o0)-.      /  _)-<  (O)_(O o)(O)    (O) (O)_(O o)
  (o)(O) ||_/ .'  ||_/ .'  (o)(O)     ||  ||  (  )    (  )   | (_))     \__ `.   / __)|^_\( \    / ) / __)|^_\
   //\\  |   /    |   /     //\\      |(__)|   )(      )(    | .-'         `. | / (   |(_))\ \  / / / (   |(_))
  |(__)| ||\ \    ||\ \    |(__)|     /.--.\  (  )    (  )   |(            _| |(  _)  |  / /  \/  \(  _)  |  /
  /,-. |(/\)\ `. (/\)\ `.  /,-. |    -'    `-  )/      )/     \)        ,-'   | \ \_  )|\\ \ `--' / \ \_  )|\\
 -'   ''     `._)     `._)-'   ''             (       (       (        (_..--'   \__)(/  \) `-..-'   \__)(/  \)


Akka Version: 2.5.0  (v2.5.0)
Akka Http Version: 10.0.5 (v10.0.5)
Application Name: paddington-test
Bind Host: 0.0.0.0
Bind Port: 8080
```


