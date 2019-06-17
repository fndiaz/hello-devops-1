# hello-devops

Essa é uma solução de deploy contínuo construída com Kubernetes (serviço EKS da AWS) e Jenkins ferramenta CI/CD
Essa stack foi escolhida pois é altamente escalável, nosso CI também garante uma grande flexibilidade para lhe dar com jobs e pipelines, e ainda evita o LockIn.

# Run UP
Instruções para subir solução

Foi criada uma pasta "devops" na raiz do projeto, essa pasta contém os arquivos de configuração para subir toda a infraestrutura na AWS, configurar o Cluster Kubernetes(EKS), subir o servdor JenkinsCI e fazer o deploy contíniuo.


1. Subindo estrutura AWS
caminhe para dentro do diretório "devops/terraform"

[ATTETION]: diponível somente nas regiões:
- us-east-1 - Virginia
- us-east-2 - Ohio
- us-west-2 - Oregon

Execute:
```
terraform init
terraform plan -var access_key=your_access_key -var secret_key=your_secret_key -var region=us-east-1
terraform apply -var access_key=your_access_key -var secret_key=your_secret_key -var region=us-east-1
```

Esse passo deve demorar alguns minutos (em trono de 10 minutos), pois a criação do cluster EKS não é tão rápida.
Na saída de comando output, teremos o enderço do nosso servidor Jenkins
```
algo parecido com isso: http://ip_jenkins:8080
```

Uma imagem base de Jenkins, com alguns plugins pré instalados e credênciais para a demo está pronta.
Mas lembrando essa é só uma imagem base, toda a configurtação de Jobs e Pipeline do Jenkisn está dentro da pasta jenkins-generator, vamos explorar isso mais pra frente.

2. Subindo Stack da Aplicação
Copie a URL do Jenkins no navegador e faça o login utilizando as credenciais para demo 
- http://ip_jenkins:8080
- user: admin
- password: admin

Na tela de Jobs, podemos ver o Job "jenkins-generator", Esse Job criará toda nossa estrtura de Jobs e Pipeline no Jenkins.
Ele executa scripts em Groovy e mantém toda nossa estrutura de Jobs centralizada e parametrizada, permitindo adicionar ou mudar configuração dos Jobs por meio de variáveis. 
O próprio Jenkins provisionando o Jenkins =]
```
Rode o job: "jenkins-generator"
```
Atualizando a tela podemos ver toda nossa estrutura de Jobs e Pipeline criados
```
Vá até a aba "Configure"
Rode o job: "config_create_configmap"
```
O job "create_configmap" atualiza o configmap do cluster kubernetes, para que os workes_nodes possam ingressar no cluster, utilizando uma rolearn já criada nos passos anteriores. 
Ele também configura o kubectl para autenticação do cluster que usa aws-iam-authenticator.

No console output depois do job ter rodado podemos ver que os nodes estão ingressados no cluster
```
ip-10-0-0-69.ec2.internal   Ready    <none>   56s   v1.12.7
ip-10-0-1-34.ec2.internal   Ready    <none>   60s   v1.12.7
Finished: SUCCESS
```
```
Ainda na aba Configure rode o job: "config_create_enviroment"
```
Esse job irá criar toda a stack de ambiente, subindo os deployments, services, configmaps, persistentvoluems do kubernetes
Esse job só irá concluir quando o loadbalance do frontend da aplicação estiver pronto
No console output do job vamos ter o endereço do frontend algo como
```
URL: http://ab2b2896b911911e99d4a0ead5bc245c-616847336.us-east-1.elb.amazonaws.com
Finished: SUCCESS
```

Neste momento a aplicação já está no ar e funcionando, acesse a URL do front e faça inserções no form
Para verificar se os dados foram enviados para o Banco, rode o Job "config_select_mysql" na aba Configure e veja a saída no "Console Output"

3. Deploy Contínuo

Cada aplicação tem sua aba de pipeline para deploy, onde é feito o build, deploy e se necessário rollback
- build: faz o buil da imagem docker atualizada e sobe para um repositório demo
- deploy: faz o deployment no kubernetes com a versão nova
- rollback: caso necessário o job volta a aplicação para a versão anterior

Obs: Caso seja necessário é possível alterar o repositório git da aplicação e dockerhub dentro de "devops/jenkins-generator/variables.dsl
Se for modificado é preciso rodar o Job "jenkins-generator", para atualizar as configurações, também será necessário atualizar as "credentials" no Jenkins.

Entre nas abas de pipeline e execute o deploy
Fique a vontade para testar o rollback

4. Destruindo ambiente

Antes de rodar o terraform destroy, rode o Job "config_delete_enviroments" que fica dentro da aba config, ele vai deletar toda a stack do kubernetes incluindo os loadbalancers da aws, coisa que o terraform não será capaz de fazer pois não foi o mesmo que os criou.

Por fim rode
```
terraform destroy -var access_key=your_access_key -var secret_key=your_secret_key -var region=us-east-1
```

Ressalvas:
Todas as aplicões foram montandas em cima de um ReplicaSet do Kubernetes, para garantir a disponibilidade e escalabilidade
O banco de dados foi montando com Persistent Volume utilizando um EBS (Elastic Block Storage) da AWS, caso o pod(container) morra os dados permanecem

Melhorias

Essa é apenas uma Demo, para ambiente em produção existem diversas melhorias que devem ser feitas, dentre elas:
1. criar uma rede privada na VPC da aws com NATGateway e VPN 
2. Centalização de configurações da aplicação utilizando "Secretes" da AWS e Configmap do kubernetes
3. Centralização dos logs utilizando fluentd ou kinesis-firehose da aws, podendo armazenar o log no elasticserach ou em um s3
4. Dashborad para visualização como prometheus e Grafana
5. Testes no pipeline

OBS[WARNING]: A imagem pública do Jenkins contém credenciais cripitografadas (github, docker) de contas que foram criadas especialmente para  esta demo. Isso foi feito apenas para demonstração, em um ambinete em produção isso seria um,a grande brecha de segurança.
O repositório também contém uma chave ssh que foi criada somente para esta demo, não é recomendado armazenar essas chaves em repositório.











