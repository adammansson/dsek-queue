# dsek-queue

Ett kösystem för pubbar som är tänkt att ersätta D-sektionens befintliga system

### Running

You need to download and install sbt for this application to run.
You also need to be using Java 11.

Once you have sbt installed, the following at the command prompt will start up Play in development mode:

```bash
sbt run
```

Play will start up on the HTTP port at <http://localhost:9000/>.   You don't need to deploy or reload anything -- changing any source code while the server is running will automatically recompile and hot-reload the application on the next HTTP request.

### Usage

If you call the same URL from the command line, you’ll see JSON. Using [httpie](https://httpie.org/), we can execute the command:

```bash
http --verbose http://localhost:9000/api
```

and get back:

```routes
GET /api HTTP/1.1
```

Likewise, you can also send a POST directly as JSON:

```bash
http --verbose POST http://localhost:9000/api content="dboll"
```

and get:

```routes
POST /v1/posts HTTP/1.1
```
