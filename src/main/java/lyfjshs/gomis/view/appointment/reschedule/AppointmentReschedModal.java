package lyfjshs.gomis.view.appointment.reschedule;

public class AppointmentReschedModal {
	private static AppointmentReschedModal instance;

	private AppointmentReschedModal() {
	// Private constructor to enforce singleton pattern
	
	}

	public static AppointmentReschedModal getInstance() {
		if (instance == null) {
			instance = new AppointmentReschedModal();
		}
		return instance;
	}

	public void showReschedModal() {
		
	}
	
	
}
