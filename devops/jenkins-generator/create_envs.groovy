//def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
//slurper.classLoader = this.class.classLoader
//def config = slurper.parse(readFileFromWorkspace('deploy.dsl'))

//config.app_job.each { name, data ->
//  println "generating application $name"
//  println name
  //println data
createJob("https://github.com/helloapp-ci/hello-devops.git", "github")
createJob2("https://github.com/helloapp-ci/hello-devops.git", "github")
createJob3("https://github.com/helloapp-ci/hello-devops.git", "github")
createView()
//}


def createJob(repo, credentials_git){

  job("config_create_enviroments") {
    scm {
        git {
        remote {
                url(repo)
                credentials(credentials_git)
              }
            branch("master")
        }
    }

    logRotator {
      daysToKeep(-1)
      numToKeep(10)
      artifactDaysToKeep(-1)
      artifactNumToKeep(-1)
    }

      steps {
      shell(getShell())
      }
  }
}


private String getShell() {

    String var_shell
    var_shell="""
cd devops/kubernetes
kubectl create -f service-hello-python.yml
kubectl create -f pv-mysql.yml
kubectl create -f configmap-script-mysql.yml
kubectl create -f deploy-mysql.yml
kubectl create -f deploy-rabbit.yml
kubectl create -f deploy-hello-python.yml
kubectl create -f service-mysql.yml
kubectl create -f service-rabbit.yml
kubectl create -f service-rabbit-web.yml
printf "Waiting ELB"
ELB=\$(kubectl get services service-hello-python -o jsonpath="{.status.loadBalancer.ingress[0].hostname}")
until \$(curl --output /dev/null --silent --head --fail http://\$ELB); do
    ELB=\$(kubectl get services service-hello-python -o jsonpath="{.status.loadBalancer.ingress[0].hostname}")
    printf 'Waiting HealthCheck'
    sleep 15
done
kubectl create -f deploy-hello-node.yml
POD=\$(kubectl get pod -l app=mysql -o jsonpath="{.items[0].metadata.name}")
kubectl exec -ti \$POD sh /usr/local/mysql-init.sh
echo "URL: http://\$ELB"
"""

  return var_shell
}



def createJob2(repo, credentials_git){

  job("config_create_configmap") {
    scm {
        git {
        remote {
                url(repo)
                credentials(credentials_git)
              }
            branch("master")
        }
    }

    logRotator {
      daysToKeep(-1)
      numToKeep(10)
      artifactDaysToKeep(-1)
      artifactNumToKeep(-1)
    }

      steps {
      shell(getShell2())
      }
  }
}


private String getShell2() {

    String var_shell
    var_shell="""
cp /tmp/config ~/.kube/
cp /tmp/aws ~/.aws/credentials
kubectl apply -f /tmp/configmap.yaml
sleep 60
kubectl get nodes
"""

  return var_shell
}

def createJob3(repo, credentials_git){

  job("config_delete_enviroments") {
    scm {
        git {
        remote {
                url(repo)
                credentials(credentials_git)
              }
            branch("master")
        }
    }

    logRotator {
      daysToKeep(-1)
      numToKeep(10)
      artifactDaysToKeep(-1)
      artifactNumToKeep(-1)
    }

      steps {
      shell(getShell3())
      }
  }
}


private String getShell3() {

    String var_shell
    var_shell="""
kubectl delete deploy deploy-mysql
kubectl delete deploy deploy-rabbit
kubectl delete deploy hello-python
kubectl delete deploy hello-node

kubectl delete service mysql
kubectl delete service rabbitmq-web
kubectl delete service rabbitmq
kubectl delete service service-hello-python

kubectl delete sc mysql-db
kubectl delete pvc mysql-db

kubectl delete cm mysql-init
kubectl delete cm mysql-select
"""

  return var_shell
}


def createJob4(repo, credentials_git){

  job("config_select_myslq") {
    scm {
        git {
        remote {
                url(repo)
                credentials(credentials_git)
              }
            branch("master")
        }
    }

    logRotator {
      daysToKeep(-1)
      numToKeep(10)
      artifactDaysToKeep(-1)
      artifactNumToKeep(-1)
    }

      steps {
      shell(getShell4())
      }
  }
}


private String getShell4() {

    String var_shell
    var_shell="""
POD=\$(kubectl get pod -l app=mysql -o jsonpath="{.items[0].metadata.name}")
kubectl exec -ti \$POD sh /usr/local/mysql-select.sh
"""

  return var_shell
}


def createView() {

  listView("Configure") {
      description("Configure")
      filterBuildQueue()
      filterExecutors()
      jobs {
          //name('release-projectA')
          regex('.*config.*')
      }
      columns {
          status()
          weather()
          name()
          lastSuccess()
          lastFailure()
          lastDuration()
          buildButton()
      }
  }

}