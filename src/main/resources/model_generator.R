library(rpart)
library(pmml)


args <- commandArgs(trailingOnly = TRUE)

if(length(args) < 2){
  stop("Needs csv and model path",call=FALSE)
}else{
  d = read.csv(args[1], header=TRUE);
  t = rpart(class~.,data=d)
  saveXML(pmml(t),args[2])
}