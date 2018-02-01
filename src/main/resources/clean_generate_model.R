library(rpart)
library(pmml)
library(jsonlite)
library(geosphere)
library(lubridate)

args <- commandArgs(trailingOnly = TRUE)

if(length(args) < 3){
  stop("Needs csv, xml and model path",call=FALSE)
}else{
  d = read.csv(args[1], header=TRUE);
  l <- fromJSON(args[2], flatten=TRUE)

  lf <- function(lt,lg, work_lt, work_lg, home_lt, home_lg){
    result <- 1;
    homed = distm (c(lg, lt), c(home_lg, home_lt), fun = distHaversine);
    workd = distm (c(lg, lt), c(work_lg, work_lt), fun = distHaversine);

    if(homed < 200){
      result <- 1
    }else{
      if(workd < 200){
        result <- 2
      }else{
        result <- 3
      }
    }

    return(result)
  }

  d$location = mapply(lf,d$latitude, d$longitude, l[1],l[2],l[3],l[4]);

  dates <- as.POSIXlt(d$timestamp)
  d$hour = hour(dates)
  d$minute = minute(dates)
  d$weekday = wday(dates)

  d = d[,c("hour","minute","weekday","foreground","activity","screen_active","call_active","music_active","ring_mode","location","context")]

  t = rpart(class~.,data=d)
  saveXML(pmml(t),args[3])

}