package homework;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import homework.dto.Statistics;
import homework.model.Event;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
public class StatisticsRestService {

	private BehaviorSubject<Map<String, Integer>> eventTypeCounter;
	private BehaviorSubject<Map<String, Integer>> wordsCounter;

	@Autowired
	private WorkService workService;
	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void init() throws IOException {
		BehaviorSubject<String> eventBehaviorSubject = BehaviorSubject.create();


		Observable<Event> eventObservable = Observable.<Event>create(emitter -> {
			eventBehaviorSubject.observeOn(Schedulers.computation()).subscribe(line -> {
				try {
					emitter.onNext( objectMapper.readValue(line, Event.class));
				} catch (JsonParseException | MismatchedInputException e) {
				}

			});
		});

		eventTypeCounter = BehaviorSubject.create();
		wordsCounter = BehaviorSubject.create();
		Observable<Map<String, Integer>> eventTypeCountObservable = addScanToObservable(
				eventObservable
				.map(Event::getEventType)
				.observeOn(Schedulers.from(Executors.newSingleThreadExecutor())));

		Observable<Map<String, Integer>> wordsCountObservable = addScanToObservable(eventObservable
				.map(Event::getData)
				.flatMap(data ->
						Observable.fromArray(data.split("\\s+")))
				.observeOn(Schedulers.from(Executors.newSingleThreadExecutor())));


		eventTypeCountObservable.subscribe(x -> {
					eventTypeCounter.onNext(x);}
				);

		wordsCountObservable.subscribe(x -> {
				wordsCounter.onNext(x);
			});
		workService.runWorker(eventBehaviorSubject);

	}

	private  Observable<Map<String, Integer>> addScanToObservable(Observable<String> observable) {
		return observable.scan(new HashMap<>(),
				(Map<String, Integer> eventMap, String string)
						->  {
					Map<String, Integer> newMap = eventMap
							.entrySet()
							.stream()
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					newMap.merge(string, 1, (x, y) -> x + y);
					return newMap;
				});
	}

	@GetMapping
	public Single<Statistics> statistics() {
		Statistics statistics = new Statistics();
		statistics.setEventTypeCounters(eventTypeCounter.getValue());
		statistics.setWordCounters(wordsCounter.getValue());
		return Single.just(statistics);
	}
}
