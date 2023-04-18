# Padl Bridge

Este projeto incentiva o uso de línguas nativas:

- [English](README.md)
- [Português do Brasil](README_pt_BR.md)

PadlBridge é uma aplicação que transfere, consolida e atualiza (ou sincroniza) dados de diversas fontes para um LDAP. Estas fontes podem ser LDAP externos, bancos de dados relacionais e arquivos de texto. Também é possivel extender facilmente sua funcionalidade para outras fontes de dados através de adaptadores.

## Pacotes

- padlcore: aplicação Java que provê os serviços de integração de dados. Pode ser utilizada em modo _standalone_, utilizando de um LDAP externo como destino e um banco de dados para controle de cache, ou integrado com uma imagem em container que provê todos os serviços.

## Como compilar

Esta aplicação usa gradle para gerar os artefatos de _release_.
