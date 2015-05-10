import java.util.Calendar;
/**
 * Created by Waide Shery on 11.05.15.
 */

public class FindNextAlarm {
    LegalDateFormat format; //переменная внутреннего класса перечислений LegalDateFormat
    /*
    Массив отражает срабатывание будильника в определенные дни недели. Отсчет ведется с понедельника.
    daysOfWeek[0] - понедельник, daysOfWeek[6] - воскресенье.
     */
    private boolean[] daysOfWeek = {false, false, false, false, false, false, false};
    private String schedule; // строка с днями срабатывания и временем
    private String dateSchedule; // строка с датой, в которую должен сработать будильник
    private String daysSchedule; // строка с днями, в которые должен сработать будильник
    private int every; // значение дня при периодичном срабатывании будильника, например, каждый 4-й день
    private String timeSchedule; // строка со значениями времени срабатывания будильника
    private int hoursSchedule = -1; // целочисленное значение часов будильника
    private int minutesSchedule = -1; // целочисленное значение минут будильника
    private int daySchedule; // целочисленное значение дня будильника
    private int monthSchedule; // целочисленное значение месяца будильника
    private int yearSchedule; // целочисленное значение года будильника

    /**
     * Конструктор с днями срабатывания и временем в одной строке.
     *
     * @param schedule Дата и время разделяются символом '@'. Например, 02.03.2015@17:00.
     * @param format   Формат даты. Если параметр принимает значение only_Time, будильник устанавливается
     *                 на каждый день в заданное время; schedule в этом случае указывается в формате "17:00"
     */
    public FindNextAlarm(String schedule, LegalDateFormat format) {
        this.format = format;
        if (format == LegalDateFormat.only_Time) {
            this.timeSchedule = schedule;
            parseTime();
        } else {
            this.schedule = schedule;
            parseSchedule();
        }
    }

    /**
     * Конструктор с датой и временем в разных строках
     *
     * @param daysSchedule Дни срабатывания. Примеры: 02.03.2015; mod,tue,sun; every-4#02.03.2015
     * @param timeSchedule Время.
     * @param format       Формат даты
     */
    FindNextAlarm(String daysSchedule, String timeSchedule, LegalDateFormat format) {
        this.daysSchedule = daysSchedule;
        this.timeSchedule = timeSchedule;
        this.format = format;
        parseDays();
        parseTime();
    }


    /**
     * Конструктор с датой в строке и временем в целочисленных переменных
     *
     * @param daysSchedule    Дни срабатывания в строке.
     * @param hoursSchedule   Часы в целочисленном формате от 0 до 23.
     * @param minutesSchedule Минуты в целочисленном формате от 0 до 59.
     * @param format          Формат даты
     */
    FindNextAlarm(String daysSchedule, int hoursSchedule, int minutesSchedule, LegalDateFormat format) {
        this.daysSchedule = daysSchedule;
        this.hoursSchedule = hoursSchedule;
        this.minutesSchedule = minutesSchedule;
        this.format = format;
        parseDays();
    }

    /**
     * Конструктор с указанием только времени в целочисленных переменных. Будильник будет устанавливаться каждый день.
     *
     * @param hoursSchedule   Часы в целочисленном формате от 0 до 23
     * @param minutesSchedule Минуты в целочисленном формате от 0 до 59.
     */
    FindNextAlarm(int hoursSchedule, int minutesSchedule) {
        this.format = LegalDateFormat.only_Time;
        this.hoursSchedule = hoursSchedule;
        this.minutesSchedule = minutesSchedule;
    }

    /**
     * Метод возвращает ближайшее время будильника в зависимости от заданной маски.
     * Вычисляется только первое значение времени после знака '@'. Если в маске указаны несколько значений
     * через знак '^', то следует использовать getSoonAlarm(int index).
     *
     * @return возвращает ближайшее время в формате UNIX-времени
     */
    public long getSoonAlarm() {
        return getDateTime();
    }

    /**
     * Метод возвращает ближайшее время будильника в зависимости от заданной маски.
     * Используется, если в маске указаны несколько значений через знак '^'.
     * Вычисляется значение времени, указанное в переменной index. Например, при маске "mod,tue,sun@17:25^18:00^19:30"
     * для вычисления ближайшего будильника времени 19:30, вызов метода будет выглядеть: getSoonAlarm(2).
     *
     * @param index Порядковый номер значения времени. Отсчет с 0.
     * @return возвращает ближайшее время в формате UNIX-времени
     */
    public long getSoonAlarm(int index) {
        parseTime(index);
        return getDateTime();
    }

    /**
     * Метод отделяет время будильника(-ов) от дней, когда он должен сработать.
     * Затем вызывает методы обработки дней и времени.
     */
    private void parseSchedule() {
        int atPos = schedule.indexOf('@');
        if (atPos == -1) {
            throw new IllegalArgumentException();
        } else {
            this.daysSchedule = schedule.substring(0, atPos);
            this.timeSchedule = schedule.substring(atPos + 1);
        }
        parseDays();
        parseTime();
    }

    /**
     * Метод определяет в каком формате указаны дни срабатывания будильника и вызывает
     * методы дальнейшей обработки
     */
    private void parseDays() {
        int sharpPos = daysSchedule.indexOf("#");
        int equalPos = daysSchedule.indexOf("=");
        if (sharpPos != -1 && equalPos != -1) {
            every = Integer.parseInt(daysSchedule.substring(equalPos + 1, sharpPos));
            dateSchedule = daysSchedule.substring(sharpPos + 1);
            parseDate();
        } else if (daysSchedule.contains(".") || daysSchedule.contains("-") || daysSchedule.contains("/")) {
            dateSchedule = daysSchedule;
            parseDate();
        } else if (format == LegalDateFormat.days_Of_The_Week) {
            parseDaysOfWeek(daysSchedule);
        } else {
            throw new IllegalArgumentException();
        }

    }

    /**
     * Метод преобразует время в формате строки в целочисленные значения часов и минут.
     * Затем вызывает метод проверки.
     *
     * @param index указывает на порядковый номер времени при указании более чем одного значения. Отсчет с 0.
     */
    private void parseTime(int index) {
        index = index * 6 + 2;
        if (timeSchedule.charAt(index) != ':') {
            throw new IllegalArgumentException();
        }
        this.hoursSchedule = Integer.parseInt(timeSchedule.substring(index - 2, index));
        this.minutesSchedule = Integer.parseInt(timeSchedule.substring(index + 1, index + 3));
        verificationTime();
    }

    /**
     * Перегруженный метод с первым значением времени.
     */
    private void parseTime() {
        parseTime(0);
    }

    /**
     * Метод проверки корректного указания значения часов и минут.
     */
    private void verificationTime() {
        if (hoursSchedule < 0 || hoursSchedule > 23) {
            throw new IllegalArgumentException();
        }
        if (minutesSchedule < 0 || minutesSchedule > 59) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Метод разбора строкового формата даты нацелочисленные значения дня, месяца и года.
     * Затем вызывает метод проверки.
     */
    private void parseDate() {
        String ch = "";
        int d = 0, m = 0, y = 0;
        switch (format) {
            case dash_D_M_YYYY:
                ch = "\\-";
                d = 0;
                m = 1;
                y = 2;
                break;
            case dash_M_D_YYYY:
                ch = "\\-";
                m = 0;
                d = 1;
                y = 2;
                break;
            case dash_YYYY_M_D:
                ch = "\\-";
                y = 0;
                m = 1;
                d = 2;
                break;
            case point_D_M_YYYY:
                ch = "\\.";
                d = 0;
                m = 1;
                y = 2;
                break;
            case point_M_D_YYYY:
                ch = "\\.";
                m = 0;
                d = 1;
                y = 2;
                break;
            case point_YYYY_M_D:
                ch = "\\.";
                y = 0;
                m = 1;
                d = 2;
                break;
            case slash_D_M_YYYY:
                ch = "/";
                d = 0;
                m = 1;
                y = 2;
                break;
            case slash_M_D_YYYY:
                ch = "/";
                m = 0;
                d = 1;
                y = 2;
                break;
            case slash_YYYY_M_D:
                ch = "/";
                y = 0;
                m = 1;
                d = 2;
                break;
        }
        String[] arrParseDate = dateSchedule.split(ch);
        daySchedule = Integer.parseInt(arrParseDate[d]);
        monthSchedule = Integer.parseInt(arrParseDate[m]);
        yearSchedule = Integer.parseInt(arrParseDate[y]);
        verificationDate();
    }

    /**
     * Метод проверки корректного указания значений дня, месяца и года.
     */
    private void verificationDate() {
        if (yearSchedule < 1902 || yearSchedule > 2037) {
            throw new IllegalArgumentException();
        }
        if (monthSchedule < 1 || monthSchedule > 12) {
            throw new IllegalArgumentException();
        }
        if (daySchedule < 1 || daySchedule > 31) {
            throw new IllegalArgumentException();
        }
        if ((daySchedule > 28 && !isLeapYear(yearSchedule)) || (daySchedule > 29 && isLeapYear(yearSchedule))) {
            throw new IllegalArgumentException();
        }
        if (daySchedule > 30 && (monthSchedule == 1 || monthSchedule == 4 ||
                monthSchedule == 6 || monthSchedule == 9 || monthSchedule == 11)) {
            throw new IllegalArgumentException();
        }
        if (daySchedule > 31 && (monthSchedule == 3 || monthSchedule == 5 || monthSchedule == 7 ||
                monthSchedule == 8 || monthSchedule == 10 || monthSchedule == 12)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Метод определения високосного года
     *
     * @param year Год
     * @return true если год високосный, в противном случае - false.
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
    }

    /**
     * Метод вычисления ближайшего значения будильника
     *
     * @return ближайшее значение будильника в UNIX-формате
     */
    private long getDateTime() {
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        long soon;
        calendar.set(Calendar.HOUR_OF_DAY, hoursSchedule);
        calendar.set(Calendar.MINUTE, minutesSchedule);
        calendar.set(Calendar.SECOND, 0);
        switch (format) {
            case only_Time:
                soon = calendar.getTimeInMillis();
                if (soon > current) {
                    return soon;
                } else {
                    return soon + 24 * 60 * 60 * 1000;
                }
            case days_Of_The_Week:
                int currentWeekDay = calendar.get(Calendar.DAY_OF_WEEK);
                soon = calendar.getTimeInMillis();
                return findSoonDayOfWeek(currentWeekDay, current, soon);
            default:
                calendar.set(yearSchedule, getMonth(monthSchedule), daySchedule);
                soon = calendar.getTimeInMillis();
                if (every != 0) {
                    while (soon <= current) {
                        soon = soon + every * 24 * 60 * 60 * 1000;
                    }
                    return soon;
                } else {
                    return soon;
                }
        }
    }

    /**
     * Метод приводит значение месяца к формату в классе Calendar
     *
     * @param month значение месяца в целочисленном формате
     * @return значение месяца в формате Calendar
     * @see java.util.Calendar
     */
    private int getMonth(int month) {
        switch (month) {
            case 1:
                return Calendar.JANUARY;
            case 2:
                return Calendar.FEBRUARY;
            case 3:
                return Calendar.MARCH;
            case 4:
                return Calendar.APRIL;
            case 5:
                return Calendar.MAY;
            case 6:
                return Calendar.JUNE;
            case 7:
                return Calendar.JULY;
            case 8:
                return Calendar.AUGUST;
            case 9:
                return Calendar.SEPTEMBER;
            case 10:
                return Calendar.OCTOBER;
            case 11:
                return Calendar.NOVEMBER;
            case 12:
                return Calendar.DECEMBER;
            default:
                return 13;
        }
    }

    /**
     * Метод определяет ближайший день недели, в который должен сработать будильник.
     *
     * @param currentWeekDay Текуший день недели.
     * @param currentTime    Текущее время в UNIX-формате.
     * @param soonTime       Время срабатывания будильника в UNIX-формате.
     * @return Новое время срабатывания будильника в UNIX-формате в зависимости от дня недели.
     */
    private long findSoonDayOfWeek(int currentWeekDay, long currentTime, long soonTime) {
        int j;
        switch (currentWeekDay) {
            case Calendar.MONDAY:
                for (int i = 0; i < 7; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.TUESDAY:
                for (int i = 1; i < 8; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.WEDNESDAY:
                for (int i = 2; i < 9; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.THURSDAY:
                for (int i = 3; i < 10; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.FRIDAY:
                for (int i = 4; i < 11; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.SATURDAY:
                for (int i = 5; i < 12; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
            case Calendar.SUNDAY:
                for (int i = 6; i < 13; i++) {
                    if (i < 7) j = i;
                    else j = i - 7;
                    if (daysOfWeek[j] && soonTime > currentTime) break;
                    else soonTime = soonTime + 24 * 60 * 60 * 1000;
                }
                break;
        }

        return soonTime;
    }

    /**
     * Метод разбирает строку с днями недели, в которые должен сработать будильник.
     * Результат записывается в массив boolean значений.
     *
     * @param week строка с днями недели
     */
    private void parseDaysOfWeek(String week) {
        week = week.toLowerCase();
        if (week.contains("mon")) {
            daysOfWeek[0] = true;
        }
        if (week.contains("tue")) {
            daysOfWeek[1] = true;
        }
        if (week.contains("wed")) {
            daysOfWeek[2] = true;
        }
        if (week.contains("thu")) {
            daysOfWeek[3] = true;
        }
        if (week.contains("fri")) {
            daysOfWeek[4] = true;
        }
        if (week.contains("sat")) {
            daysOfWeek[5] = true;
        }
        if (week.contains("sun")) {
            daysOfWeek[6] = true;
        }
    }

    /*
     * Класс перечисления, в котором указывается формат даты
     * Вначале каждой переменной указан разделительный знак в дате: 'slash' - слэш, 'dash' - тире, 'point' - точка
     * 'M' - месяц в цифровом формате; допустимы форматы вида '05' или '5'.
     * 'D' - день в цифровом формате; допустимы форматы вида '09' или '9'.
     * 'YYYY' - 4-символьное обозначение года, например, 2017.
     *  only_Time - указывается только время, будильник устанавливается на каждый день
     */
    public enum LegalDateFormat {
        slash_M_D_YYYY, slash_D_M_YYYY, slash_YYYY_M_D,
        dash_M_D_YYYY, dash_D_M_YYYY, dash_YYYY_M_D,
        point_M_D_YYYY, point_D_M_YYYY, point_YYYY_M_D,
        only_Time, days_Of_The_Week
    }

}

