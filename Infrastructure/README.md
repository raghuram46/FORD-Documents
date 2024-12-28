## How to deploy a Cloud Run instance
In order to deploy a Cloud Run instance to GCloud you must first have access to a GCloud project
in which your userDTO can deploy Cloud Run, you will also need Docker installed locally to build your
cloud run image, and finally you will need terraform to deploy it all.

Once you have the above set up you can follow the steps below to build and deploy
your Cloud Run instance.

1. In the root of this project, run gradle build
   1. ``./gradlew clean build``
2. Once the project is successfully built you can create a
Docker container to push to GCloud using the following commands:
   1. ``docker build . -t gcr.io/<project_id>/<image_name>``
   3. ``gcloud auth login``
   4. ``gcloud auth configure-docker`` - only needed if not configured previously
   5. ``docker push gcr.io/<project_id>/<image_name>`` - this publishes the image to your GCloud project
3. Then you can deploy your newly created docker image to Cloud Run with the following steps:
   1. First check out the **terraform.tfvars** file and replace any variables you need
   2. Run ``terraform init`` - if this is the first time deploying
   3. Run ``terraform apply`` - this will attempt to create all of the resources in GCloud
   4. For any updates to the Cloud Run image, you can run terraform apply once you have made changes
   to the terraform files / variables and it will update what is already deployed.

 