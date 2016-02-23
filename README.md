## XPages Fusion Application Project

This project contains an XPages application designed to run on Bluemix that demonstrates how to use a number of different services from the Bluemix catalog. The initial set of services includes: Cloudant DB, Watson Language Translation, Watson Text to Speech, and Watson Image Recognition.

[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/OpenNTF/XPages-Fusion-Application)

The procedure to use the above “Deploy to Bluemix” button is as follows:

 - Ensure that you are registered on Bluemix, and that your account is
   active 
 - Click the “Deploy to Bluemix” button
 - After the deployment page has loaded, login to Bluemix if prompted
 - Enter an application name
 - Choose the region, organization and space to which the application will be deployed
 - Click “Deploy”

Now sit back and relax as Bluemix will do the rest. It will do the following automatically:

 - Create a new repository on http://hub.jazz.net associated with your Bluemix account
 - Clone the XPages Fusion Application repository there
 - Configure the build pipeline of that repository
 - Create a new XPages application in your Bluemix organization
 - Connect the application to your jazz repository
 - Create four new services instances
 - Bind the services to your new application
 - Copy the xpagesFusion.nsf file to your new app
 - Start up your application

The whole process takes about 5 minutes, then you can try out your new XPages Fusion Application running on Bluemix, clone the jazzhub repository to your machine to examine the code, and start making changes to the application.
