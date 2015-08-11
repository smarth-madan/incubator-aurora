/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.aurora.scheduler.http;

import java.util.Locale;
import java.util.logging.Level;

import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import com.twitter.common.testing.easymock.EasyMockTest;
import com.twitter.common.util.testing.FakeClock;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.DateCache;
import org.junit.Before;
import org.junit.Test;

import static org.apache.aurora.scheduler.http.RequestLogger.LogSink;
import static org.easymock.EasyMock.expect;

public class RequestLoggerTest extends EasyMockTest {

  private DateCache logDateCache;
  private FakeClock clock;
  private LogSink sink;
  private Request request;
  private Response response;

  private RequestLog log;

  @Before
  public void setUp() throws Exception {
    logDateCache = new DateCache("dd/MMM/yyyy:HH:mm:ss Z", Locale.getDefault());
    logDateCache.setTimeZoneID("GMT");
    clock = new FakeClock();
    sink = createMock(LogSink.class);
    request = createMock(Request.class);
    response = createMock(Response.class);
    log = new RequestLogger(clock, sink);
  }

  @Test
  public void testFormat200() throws Exception {
    clock.advance(Amount.of(40L * 365, Time.DAYS));

    expect(response.getStatus()).andReturn(200).atLeastOnce();
    expect(request.getServerName()).andReturn("snoopy");
    expect(request.getHeader(HttpHeaders.X_FORWARDED_FOR)).andReturn(null);
    expect(request.getMethod()).andReturn("GET");
    expect(request.getUri()).andReturn(new HttpURI("/"));
    expect(request.getProtocol()).andReturn("http");
    expect(response.getContentCount()).andReturn(256L);
    expect(request.getRemoteAddr()).andReturn("easymock-test");
    expect(request.getHeader(HttpHeaders.REFERER)).andReturn(null);
    expect(request.getHeader(HttpHeaders.USER_AGENT)).andReturn("junit");
    expect(request.getTimeStamp()).andReturn(clock.nowMillis()).atLeastOnce();

    expect(sink.isLoggable(Level.INFO)).andReturn(true);

    String logDate = logDateCache.format(clock.nowMillis());
    sink.log(Level.INFO, "snoopy easymock-test [" + logDate + "]"
        + " \"GET / http\" 200 256 \"-\" \"junit\" 110");

    control.replay();

    clock.advance(Amount.of(110L, Time.MILLISECONDS));
    log.log(request, response);
  }

  @Test
  public void testFormat500() throws Exception {
    clock.advance(Amount.of(40L * 365, Time.DAYS));

    expect(response.getStatus()).andReturn(500).atLeastOnce();
    expect(request.getServerName()).andReturn("woodstock");
    expect(request.getHeader(HttpHeaders.X_FORWARDED_FOR)).andReturn(null);
    expect(request.getMethod()).andReturn("POST");
    expect(request.getUri()).andReturn(new HttpURI("/data"));
    expect(request.getProtocol()).andReturn("http");
    expect(response.getContentCount()).andReturn(128L);
    expect(request.getRemoteAddr()).andReturn("easymock-test");
    expect(request.getHeader(HttpHeaders.REFERER)).andReturn(null);
    expect(request.getHeader(HttpHeaders.USER_AGENT)).andReturn("junit");
    expect(request.getTimeStamp()).andReturn(clock.nowMillis()).atLeastOnce();

    expect(sink.isLoggable(Level.WARNING)).andReturn(true);

    String logDate = logDateCache.format(clock.nowMillis());
    sink.log(Level.WARNING, "woodstock easymock-test [" + logDate + "]"
        + " \"POST /data http\" 500 128 \"-\" \"junit\" 500");

    control.replay();

    clock.advance(Amount.of(500L, Time.MILLISECONDS));
    log.log(request, response);
  }
}
