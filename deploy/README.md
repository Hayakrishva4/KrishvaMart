# Deployment reference files (Section 10)

These mirror the reference setup in Section 10 of the spec, adjusted for
the actual paths on whatever VM you provision:

- `krishvamart-h2.service` - runs H2 in server mode as a systemd service
  (step 5). Set `H2_JAR` to wherever the H2 jar actually lives on the box.
- `tomcat.service` - runs Tomcat 9 as a systemd service (step 3), depends on
  the H2 service above so it starts first.
- `nginx-krishvamart.conf` - reverse proxy with TLS in front of Tomcat's
  port 8080 (step 4).

## Install steps on a fresh Linux VM

```bash
sudo apt update && sudo apt install -y openjdk-17-jdk nginx
# ... install Tomcat 9.0.x under /opt/tomcat9, copy the H2 jar to /opt/krishvamart/lib/h2.jar ...

sudo cp deploy/krishvamart-h2.service /etc/systemd/system/
sudo cp deploy/tomcat.service /etc/systemd/system/
sudo cp deploy/nginx-krishvamart.conf /etc/nginx/sites-available/krishvamart
sudo ln -s /etc/nginx/sites-available/krishvamart /etc/nginx/sites-enabled/

sudo systemctl daemon-reload
sudo systemctl enable --now krishvamart-h2
sudo systemctl enable --now tomcat
sudo nginx -t && sudo systemctl reload nginx
```

Then build and deploy the WAR:
```bash
mvn clean package
cp target/krishvamart.war /opt/tomcat9/webapps/krishvamart.war
```

Backup requirement (Section 10): after each review, copy the H2 data file
(`*.mv.db` under the `baseDir` configured in `krishvamart-h2.service`)
somewhere durable, and re-record a fresh 2-3 minute screen-capture demo.
