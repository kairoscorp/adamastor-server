all: clean compile

clean: 
	mvn clean install

compile:
	mvn compile

publish:
	mvn heroku:deploy
	
