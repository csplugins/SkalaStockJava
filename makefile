all:
	javac -target 1.6 -source 1.6 -cp .:* Main.java -nowarn
	java -cp .:* Main
jar:
	rm -f Main.jar
	rm -rf /home/cody/Desktop/StockProgram/*
	rm -rf /home/cody/Desktop/StockProgram.zip
	javac -target 1.6 -source 1.6 -cp .:* Main.java -nowarn
	jar cvfm Main.jar manifest.txt *.class
	chmod +x Main.jar
	cp -R Main.jar commons-io.jar YahooFinanceAPI.jar htmlHelper/ /home/cody/Desktop/StockProgram/
	cd /home/cody/Desktop/; zip -r /home/cody/Desktop/StockProgram.zip StockProgram/
