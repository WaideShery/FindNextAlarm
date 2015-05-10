/**
 * Created by Waide Shery on 11.05.15.
 */
public class Main {
    public static void main(String[] args) {
        FindNextAlarm alarm;
        alarm = new FindNextAlarm("26.05.2015@22:00", FindNextAlarm.LegalDateFormat.point_D_M_YYYY);
        System.out.println("1)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("5/26/2015@22:00", FindNextAlarm.LegalDateFormat.slash_M_D_YYYY);
        System.out.println("2)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("2015-05-26@22:00^23:00", FindNextAlarm.LegalDateFormat.dash_YYYY_M_D);
        System.out.println("3)Время будильника: " + alarm.getSoonAlarm());
        System.out.println("3)Время второго будильника: " + alarm.getSoonAlarm(1));

        alarm = new FindNextAlarm("sun, sat@22:00", FindNextAlarm.LegalDateFormat.days_Of_The_Week);
        System.out.println("4)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("22:00", FindNextAlarm.LegalDateFormat.only_Time);
        System.out.println("5)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("every=4#01.01.2015@22:00", FindNextAlarm.LegalDateFormat.point_D_M_YYYY);
        System.out.println("6)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("01.01.2016", "22:00", FindNextAlarm.LegalDateFormat.point_D_M_YYYY);
        System.out.println("7)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm("mon, tue, wed, thu, fri", 6, 30, FindNextAlarm.LegalDateFormat.days_Of_The_Week);
        System.out.println("8)Время будильника: " + alarm.getSoonAlarm());

        alarm = new FindNextAlarm(22, 35);
        System.out.println("9)Время будильника: " + alarm.getSoonAlarm());
    }
}
