# Consultor de Moedas Pro — Versão com Permissão Android

## Descrição
Este aplicativo consiste em um consultor de cotação de moedas em tempo real que consome uma API pública. O projeto foi evoluído a partir da atividade anterior para implementar boas práticas de privacidade, segurança e controle de permissões em tempo de execução na plataforma Android.

## Relação com a atividade anterior
Na primeira versão, o miniapp utilizava apenas a permissão padrão de acesso à rede (INTERNET) para buscar e exibir os dados brutos na tela. Nesta versão atualizada, foi introduzida uma nova funcionalidade que permite ao usuário disparar e receber um alerta visual com o valor da cotação diretamente na gaveta de notificações do dispositivo.

## API utilizada
- Nome da API: AwesomeAPI (Economia)
- Endpoint utilizado: `https://economia.awesomeapi.com.br/json/last/{MOEDA}-BRL`
- Dados exibidos no app: Nome completo da moeda, valor de compra e valor de venda.

## Permissão Android utilizada
- Permissão escolhida: `android.permission.POST_NOTIFICATIONS`
- Onde ela foi declarada no Manifest: Declarada explicitamente por meio da tag `<uses-permission>` posicionada logo acima do bloco `<application>` principal.
- Por que essa permissão é necessária para o app: Ela é necessária para habilitar o envio de alertas visuais e notificações do sistema contendo os dados comerciais das moedas na barra de status do aparelho.
- Em qual momento do fluxo ela é solicitada ao usuário: A solicitação em tempo de execução (runtime permission) é disparada no exato momento em que o usuário clica no botão verde "Notificar esta Cotação".

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
