job("task6_job1"){
        description("This will copy code from github & filter and send it to different directories present in the minikube based on file extension")
        scm {
                 github('rakhi8939/devopstask3' , 'master')
             }
        triggers {
                scm("* * * * *")
                
  	}
        steps {
        shell('''sudo cp -rvf * /root/task
        ''')
      }
}


job("task6_job2"){
        description("check the html server , launch it and deploy html file")
        
        triggers {
        upstream {
    upstreamProjects("task6_job1")
    threshold("Fail")
        }
        }
        steps {
        shell('''if sudo kubectl get pod | grep httpd-webserver
then
echo "httpd webserver already running"
else
sudo kubectl create -f /root/task3/httpd.yml
fi
''')
      }
}


job("task6_job3"){
        description("check the php server , launch it and deploy the php file ")
        
        triggers {
                upstream {
    upstreamProjects("task6_job2")
    threshold("Fail")
   } 
        }
        steps {
        shell('''if sudo kubectl get pods | grep php-webserver
then
echo "php-server already running fine"
else
sudo kubectl create -f /root/task3/php.yml
fi
''')
      }
}

job("task6_job4"){
        description("email notification")
         triggers {
                upstream {
    upstreamProjects("task6_job3")
    threshold("Fail")
   } 
         }
        steps {
        shell('''status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.102:32145/bts.html)
echo $status
if [[$status==200]]
then
echo "All Good"
else
sudo python3 /root/mail.py
fi

status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.102:32146/bts.php)
echo $status
if [[$status==200]]
then
echo "All Good"
else
sudo python3 /root/mail.py
fi
''')
      }
}

buildPipelineView('DevOps-task6-pipeline'){
    filterBuildQueue()
    filterExecutors()
    title('CI/CD Pipeline')
    displayedBuilds(3)
    selectedJob('task6_job1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(5)
}
