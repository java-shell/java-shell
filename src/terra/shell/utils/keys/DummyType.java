package terra.shell.utils.keys;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public class DummyType implements EventType {

	@Override
	public Event createEvent(EventInformation data) {
		return new DummyEvent(data);
	}

	@Override
	public String type() {
		// TODO Auto-generated method stub
		return "DummyType";
	}

	public class DummyEvent implements Event {
		Logger log = LogManager.getLogger("DummyEvent");
		EventInformation data;

		@EventPriority(id = "Dummy", value = 0)
		public DummyEvent(EventInformation data) {
			this.data = data;
		}

		public EventInformation getData() {
			return data;
		}

		public String getCreator() {
			// TODO Auto-generated method stub
			return "Dummy";
		}

	}

}
