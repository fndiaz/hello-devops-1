app_job {
  'hello-python' {
    url = 'https://github.com/helloapp-ci/hello-devops.git'
    branch = 'master'
    rotate_builds = 10
    pos_build = 'deploy-hello-node'
    credentials_docker = 'dockerhub'
    credentials_git = 'github'
    user_dockerhub = 'helloapp'
  }

  'hello-node' {
    url = 'https://github.com/helloapp-ci/hello-devops.git'
    branch = 'master'
    rotate_builds = 10
    pos_build = 'deploy-hello-node'
    credentials_docker = 'dockerhub'
    credentials_git = 'github'
    user_dockerhub = 'helloapp'
  }

}

list_views {
  'View Builds'{
    description = "builds jobs"
    regex = '.*build.*'
  }
  'View Deploy'{
    description = "deploy jobs"
    regex = '.*deploy.*'
  }
  'View Rollback'{
    description = "rollback jobs"
    regex = '.*rollback.*'
  }
}