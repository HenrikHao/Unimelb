# COMP90024 Project
This repository hosts the source code for COMP90024 Project Team 1, dedicated to exploring the correlations between bushfires, air quality, and respiratory illnesses. Our investigation focuses primarily on Australia, with a specific emphasis on the New South Wales region.

Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)

## Structure
```bash
├── frontend
├── backend
│   ├── fission
│   └── mastodon-harvestor
├── test
├── database
├── data
├── docs
│   ├── report
│   └── API_documentation
└── README.md
```
- [__frontend__](./frontend/): source code of the client part of the application (Jupyter notebooks)
- [__backend__](./backend/): the application back-end source code (harvesters, fission functions, etc)
- [__test__](./test/): the application back-end automated tests source code
- [__database__](./database/): interactions with ElasticSearch
- [__data__](./data/): csv datasets uploaded to ElasticSearch
- [__docs__](./docs/): documentations (project report, docs on the API of backend, artefacts)

## Installation Instruction
| Package | Installation Command |
|------------------|----------------------|
| Package managers | - macOS and Linux: [Homebrew](https://brew.sh/) <br> - Ubuntu: APT| 
| jq | - HomeBrew: `brew install jq` <br> - Ubuntu: `sudo apt install jq` |
| asdf | __HomeBrew__ <br> `brew install asdf`<br>`echo -e "\n. $(brew --prefix asdf)/libexec/asdf.sh" >> ~/.profile`<br>`echo -e "\n. $(brew --prefix asdf)/etc/bash_completion.d/asdf.bash" >> ~/.profile` <br><br> __Ubuntu__ <br> `sudo apt install curl git`<br>`git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0`<br>`(echo; echo '. "$HOME/.asdf/asdf.sh"') >> ~/.bashrc`<br>`(echo; echo '. "$HOME/.asdf/completions/asdf.bash"') >> ~/.bashrc` |
| asdf plugins | `asdf plugin add kubectl`<br>`asdf plugin add helm`<br>`asdf plugin add fission` |
| kubectl (1.26.8) | `asdf install kubectl 1.26.8`<br>`asdf local kubectl 1.26.8` |
| Helm (3.6.3) | `asdf install helm 3.6.3`<br>`asdf local helm 3.6.3` |
| Fission (1.20.0) | `asdf install fission 1.20.0`<br>`asdf local fission 1.20.0` |


## Instruction on Using Client
1. OpenStack RC file, API password, and a key pair is [obtained](https://gitlab.unimelb.edu.au/feit-comp90024/comp90024/-/blob/master/installation/README.md#client-configuration) from Melbourne Research Cloud.
2. Source the OpenStack RC file 
    ```shell
    source <path-to-openstack-rc-file>
    ```
3. The public key of the key pair generated in the previous step is added to the project's Kubernetes bastion node.
4. Connect to [Campus network](https://studentit.unimelb.edu.au/wifi-vpn#uniwireless) if on-campus or [UniMelb Student VPN](https://studentit.unimelb.edu.au/wifi-vpn#vpn) if off-campus
5. Access Kubernetes cluster
    ```shell
    chmod 600 <path-to-private-key> (e.g. ~/Downloads/mykeypair.pem)

    ssh -i <path-to-private-key> (e.g. ~/Downloads/mykeypair.pem) -L 6443:$(openstack coe cluster show elastic -f json | jq -r '.master_addresses[]'):6443 ubuntu@$(openstack server show bastion -c addresses -f json | jq -r '.addresses["qh2-uom-internal"][]')
    ```
6. Access ElasticSearch (in a new terminal)
    ```shell
    kubectl port-forward service/elasticsearch-master -n elastic 9200:9200
    ```
7. Access Fisson (in a new terminal)
    ```shell
    kubectl port-forward service/router -n fission 9090:80
    ```
8. Access data through [RESTful API](./docs/API_Documentation.pdf)