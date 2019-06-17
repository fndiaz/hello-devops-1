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
createView()
//}


def createJob(repo, credentials_git){

  job("create_envs") {
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

  job("create_configmap") {
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
kubectl apply -f /tmp/configmap.yaml
sleep 60
kubectl get nodes
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
          regex('.*create.*')
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