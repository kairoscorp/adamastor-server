import datetime
import random
import xml.etree.ElementTree as ET
import csv
import sys


class row_generator:

    def __init__(self, config_file):
        tree = ET.parse(config_file)
        root = tree.getroot()

        self.days = int(root.find("days").text)
        self.period = int(root.find("period").text)

        work_root = root.find("work")
        sleep_root = root.find("sleep")
        leisure_root = root.find("leisure")
        travel_root = root.find("travel")
        activity_root = root.find("activity")

        self.phonecall_activity = int(activity_root.find("phone").text)
        self.music_activity = int(activity_root.find("music").text)

        # --------------------------------------------------------------------------------------------------------------
        # -------------------------------------------WORK---------------------------------------------------------------
        # --------------------------------------------------------------------------------------------------------------

        self.work_start_morning = int(work_root.find("work_start_morning").text)
        self.work_end_morning = int(work_root.find("work_end_morning").text)
        self.work_start_lunch = int(work_root.find("work_start_lunch").text)
        self.work_end_lunch = int(work_root.find("work_end_lunch").text)
        self.work_start_afternoon = int(work_root.find("work_start_afternoon").text)
        self.work_end_afternoon = int(work_root.find("work_end_afternoon").text)
        self.phonecall_n_w = float(work_root.find("phonecall").text)
        self.music_n_w = float(work_root.find("music").text)
        self.music_l_w = float(work_root.find("music_l").text) / self.period

        self.activity_n_w = int(work_root.find("activities").text)
        self.activity_l_w = float(work_root.find(
            "activity_l").text) / self.period
        self.activity_q_w = int(work_root.find("activity_q").text)

        self.activities_w = []
        for activity in work_root.findall("activity"):
            self.activities_w.append(int(activity.text))

        self.work_duration = self.get_work_duration()
        self.activity_c_w = (self.activity_n_w /
                             self.work_duration) * self.period
        self.music_c_w = (self.music_n_w / self.work_duration) * self.period
        self.phonecall_w = (self.phonecall_n_w /
                            self.work_duration) * self.period

        for movement in work_root.findall("movement"):
            if(movement.get("type") == "walk"):
                self.walk_p_w = float(movement.text)
                self.walk_c_w = self.walk_p_w / 100
            if (movement.get("type") == "still"):
                self.still_p_w = float(movement.text)
                self.still_c_w = self.still_p_w / 100
            if (movement.get("type") == "vehicle"):
                self.vehicle_p_w = float(movement.text)
                self.vehicle_c_w = self.vehicle_p_w / 100

        self.movement_b1_w = self.still_c_w
        self.movement_b2_w = self.still_c_w + self.walk_c_w

        # --------------------------------------------------------------------------------------------------------------
        # -------------------------------------------LEISURE------------------------------------------------------------
        # --------------------------------------------------------------------------------------------------------------

        self.leisure_start_morning = int(leisure_root.find("leisure_start_morning").text)
        self.leisure_end_morning = int(leisure_root.find("leisure_end_morning").text)
        self.leisure_start_afternoon = int(leisure_root.find("leisure_start_afternoon").text)
        self.leisure_end_afternoon = int(leisure_root.find("leisure_end_afternoon").text)
        self.phonecall_n_l = float(leisure_root.find("phonecall").text)
        self.music_n_l = float(leisure_root.find("music").text)
        self.music_l_l = int(leisure_root.find("music_l").text) / self.period

        self.activity_n_l = int(leisure_root.find("activities").text)
        self.activity_l_l = int(leisure_root.find(
            "activity_l").text) / self.period
        self.activity_q_l = int(leisure_root.find("activity_q").text)

        self.activities_l = []
        for activity in leisure_root.findall("activity"):
            self.activities_l.append(int(activity.text))

        self.leisure_duration = self.get_leisure_duration()
        self.activity_c_l = (self.activity_n_l /
                             self.leisure_duration) * self.period
        self.music_c_l = (self.music_n_l / self.leisure_duration) * self.period
        self.phonecall_l = (self.phonecall_n_l /
                            self.leisure_duration) * self.period

        for movement in leisure_root.findall("movement"):
            if(movement.get("type") == "walk"):
                self.walk_p_l = float(movement.text)
                self.walk_c_l = self.walk_p_l / 100
            if (movement.get("type") == "still"):
                self.still_p_l = float(movement.text)
                self.still_c_l = self.still_p_l / 100
            if (movement.get("type") == "vehicle"):
                self.vehicle_p_l = float(movement.text)
                self.vehicle_c_l = self.vehicle_p_l / 100

        self.movement_b1_l = self.still_c_l
        self.movement_b2_l = self.still_c_l + self.walk_c_l

        # --------------------------------------------------------------------------------------------------------------
        # -------------------------------------------TRAVEL-------------------------------------------------------------
        # --------------------------------------------------------------------------------------------------------------

        self.phonecall_n_t = float(travel_root.find("phonecall").text)
        self.music_n_t = float(travel_root.find("music").text)
        self.music_l_t = int(travel_root.find("music_l").text) / self.period

        self.activity_n_t = float(travel_root.find("activities").text)
        self.activity_l_t = int(travel_root.find(
            "activity_l").text) / self.period
        self.activity_q_t = int(travel_root.find("activity_q").text)

        self.activities_t = []
        for activity in travel_root.findall("activity"):
            self.activities_t.append(int(activity.text))

        self.travel_duration = self.get_travel_duration()
        self.phonecall_t = (self.phonecall_n_t /
                            self.travel_duration) * self.period
        self.activity_c_t = (self.activity_n_t /
                             self.travel_duration) * self.period
        self.music_c_t = (self.music_n_t / self.travel_duration) * self.period

        for movement in travel_root.findall("movement"):
            if(movement.get("type") == "walk"):
                self.walk_p_t = float(movement.text)
                self.walk_c_t = self.walk_p_t / 100
            if (movement.get("type") == "still"):
                self.still_p_t = float(movement.text)
                self.still_c_t = self.still_p_t / 100
            if (movement.get("type") == "vehicle"):
                self.vehicle_p_t = float(movement.text)
                self.vehicle_c_t = self.vehicle_p_t / 100

        self.movement_b1_t = self.still_c_t
        self.movement_b2_t = self.still_c_t + self.walk_c_t

        # --------------------------------------------------------------------------------------------------------------
        # -------------------------------------------SLEEP--------------------------------------------------------------
        # --------------------------------------------------------------------------------------------------------------
        self.sleep_start = int(sleep_root.find("start").text)
        self.sleep_end = int(sleep_root.find("end").text)

        self.reset_events()

    def reset_events(self):

        self.in_phonecall = 0
        self.call_duration = 0
        self.in_music = 0
        self.music_duration = 0
        self.music_this_mode = 0
        self.last_context = 0
        self.music_select_duration = 0
        self.in_activity = 0
        self.activity_duration = 0
        self.movement_duration = 0
        self.movement = 1
        self.activity_shift = 0
        self.activity = 0

    def generate_logfile(self, file_path):
        file = open(file_path, 'w')
        header = ["hour", "minute", "weekday", "foreground", "activity", "screen_active", "call_active", "music_active",
                  "ring_mode", "location", "class"]

        writer = csv.writer(file, dialect='excel')
        writer.writerow(header)

        self.weekday = 1

        for i in range(self.days):
            self.generate_day(writer)
            self.weekday = (self.weekday % 7) + 1
        file.close()

    def generate_day(self, writer):

        date = datetime.datetime(2017, 1, 1, 0, 0, 0, 0)
        end_date = datetime.datetime(2017, 1, 2, 0, 0, 0, 0)

        while(date < end_date):
            context = self.get_context(date.hour, date.minute)

            if(context != self.last_context):
                self.music_this_mode = 0
                self.last_context = context

            if(context == 0):
                self.reset_events()
                writer.writerow(
                    [str(date.hour), str(date.minute), str(self.weekday), "0", "1", "0",
                     "0", "0", "1", "1", context])
            else:
                if(context == 1):
                    self.event_handler(self.phonecall_l, self.music_c_l, self.music_l_l, self.activity_c_l,
                                       self.activity_l_l, self.activity_q_l, self.activities_l, self.movement_b1_l, self.movement_b2_l)
                if(context == 2):
                    self.event_handler(self.phonecall_t, self.music_c_t, self.music_l_t, self.activity_c_t,
                                       self.activity_l_t, self.activity_q_t, self.activities_t, self.movement_b1_t, self.movement_b2_t)
                if(context == 3):
                    self.event_handler(self.phonecall_w, self.music_c_w, self.music_l_w, self.activity_c_w,
                                       self.activity_l_w, self.activity_q_w, self.activities_w, self.movement_b1_w, self.movement_b2_w)

                writer.writerow([str(date.hour), str(date.minute), str(self.weekday), self.activity,
                                 self.movement, str(self.is_screen_active()), str(
                                     self.in_phonecall), str(self.in_music), "1",
                                 str(self.get_location(context)), context])

            date += datetime.timedelta(0, self.period)

    '''
        0 - sleep
        1 - leisure
        2 - travel
        3 - work
    '''

    def get_context(self, hour, minute):

        if((self.sleep_start > self.sleep_end and (hour >= self.sleep_start or hour < self.sleep_end))
           or (self.sleep_start < self.sleep_end and hour >= self.sleep_start and hour < self.sleep_end)):
            return 0

        if(hour >= self.leisure_start_morning and hour < self.leisure_end_morning):
            return 1

        if (hour >= self.work_start_morning and hour < self.work_end_morning):
            return 3

        if(hour >= self.work_start_lunch and hour < self.work_end_lunch):
            return 1

        if (hour >= self.work_start_afternoon and hour < self.work_end_afternoon):
            return 3

        if (hour >= self.leisure_start_afternoon and hour < self.leisure_end_afternoon):
            return 1

        return 2

    def event_handler(self, phonecall_chance, music_chance, music_length, activity_chance, activity_length,
                      activity_quantity, activities,  b1, b2):

        if(self.in_phonecall == 0):
            if(self.randomevent(phonecall_chance)):
                self.in_phonecall = 1
                self.call_duration = (5 * 60) / self.period
            else:
                if(self.in_activity == 0 and self.randomevent(activity_chance)):
                    self.in_activity = 1
                    self.activity_duration = activity_length * activity_quantity
                    self.activity_shift = activity_length
                    self.activity = self.get_activity(activities)
                else:
                    if (self.in_music == 0 and self.music_this_mode == 0 and self.randomevent(music_chance)):
                        self.in_music = 1
                        self.music_select_duration = 60 / self.period
                        self.music_this_mode = 1
                        self.music_duration = music_length

        if(self.movement_duration <= 0):
            self.movement = self.get_movement(b1, b2)
            self.movement_duration = 30 / self.period

        if(self.music_duration == 1):
            self.music_select_duration = 2

        if (self.music_duration <= 0):
            self.in_music = 0

        if (self.call_duration <= 0):
            self.in_phonecall = 0

        if (self.activity_duration <= 0):
            self.in_activity = 0

        if (self.in_phonecall or self.music_select_duration > 0):
            self.activity = self.get_activity(activities)

        if (self.activity_shift <= 0 and self.activity_duration > 0):
            self.activity_shift = activity_length
            self.activity = self.get_activity(activities)

        if(self.in_activity == 0 and self.in_phonecall == 0 and self.music_select_duration <= 0):
            self.activity = 0

        self.music_duration -= 1
        self.music_select_duration -= 1
        self.call_duration -= 1
        self.movement_duration -= 1
        self.activity_duration -= 1
        self.activity_shift -= 1

    def get_activity(self, activities):
        if(self.in_phonecall):
            return self.phonecall_activity

        if(self.music_duration > 0):
            return self.music_activity

        activities_index = random.randint(0, (len(activities)-1))

        return activities[activities_index]

    def randomevent(self, chance):
        x = random.random()

        if(x > chance):
            return 0
        else:
            return 1

    def is_screen_active(self):
        if(self.in_phonecall == 1 or self.in_activity == 1 or self.music_select_duration > 0):
            return 1
        else:
            return 0

    def get_work_duration(self):
        morning = (self.work_end_morning - self.work_start_morning) * 60 * 60
        afternoon = (self.work_end_afternoon - self.work_start_afternoon) * 60 * 60

        return morning + afternoon

    def get_leisure_duration(self):
        morning = (self.leisure_end_morning - self.leisure_start_morning) * 60 * 60
        lunch = (self.work_end_lunch - self.work_start_lunch) * 60 * 60
        afternoon = (self.leisure_end_afternoon - self.leisure_start_afternoon) * 60 * 60

        return morning + lunch + afternoon

    def get_travel_duration(self):
        morning = (self.work_start_morning - self.leisure_end_morning) * 60 * 60
        afternoon = (self.leisure_start_afternoon - self.work_end_morning) * 60 * 60

        return morning + afternoon

    def get_location(self, context):

        if(context == 0):
            return 1
        if(context == 1):
            return 1
        if(context == 2):
            return 3

        return 2

    def get_movement(self, b1, b2):

        x = random.random()

        if(x <= b1):
            return 1
        if(x <= b2):
            return 2

        return 3


if __name__ == "__main__":
    log_generator = row_generator(str(sys.argv[1]))
    log_generator.generate_logfile(str(sys.argv[2]))
