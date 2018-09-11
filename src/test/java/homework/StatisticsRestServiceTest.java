package homework;


import homework.dto.Statistics;
import homework.model.Event;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatisticsRestServiceTest {
	public static int MAGIC_NUMBER = 42;

	@Autowired
	private WebTestClient webTestClient;

	@Configuration
	@Import(Application.class)
	public static class TestConfig {
		@Bean
		public WorkService workService() {
			return new WorkService(){
				public void runWorker(BehaviorSubject<String> behaviorSubject) {
					behaviorSubject.onNext("{ \"event_type\": \"foo\", \"data\": \"lorem\", \"timestamp\": 1536683400 }");
					behaviorSubject.onNext("{ \"�����\u0006��");
					behaviorSubject.onNext("");
					behaviorSubject.onNext("{ \"event_type\": \"foo\", \"data\": \"lorem\", \"timestamp\": 1536683400 }");
					behaviorSubject.onNext("{ \"event_type\": \"foo\", \"data\": \"dolor\", \"timestamp\": 1536683400 }");
					behaviorSubject.onNext("{ \"event_type\": \"baz\", \"data\": \"amet\", \"timestamp\": 1536683394 }");
					behaviorSubject.onNext("{ \"event_type\": \"baz\", \"data\": \"amet\", \"timestamp\": 1536683376 }");
				}
			};
		}
	}

	@Test
	public void testHello() {
		webTestClient
			.get().uri("/statistics")
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Statistics.class).consumeWith(e -> {
					Statistics actualStatistics = e.getResponseBody();
					Assert.assertEquals(3, actualStatistics.getEventTypeCounters().get("foo").intValue());
					Assert.assertEquals(2, actualStatistics.getEventTypeCounters().get("baz").intValue());
					Assert.assertEquals(2, actualStatistics.getWordCounters().get("lorem").intValue());
					Assert.assertEquals(2, actualStatistics.getWordCounters().get("amet").intValue());
					Assert.assertEquals(1, actualStatistics.getWordCounters().get("dolor").intValue());
				});
	}
}
