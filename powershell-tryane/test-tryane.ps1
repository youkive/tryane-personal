# Load DDL
Add-Type -Path .\lib\Microsoft.IdentityModel.Clients.ActiveDirectory.dll

$appIdConnectorSP = 'f94418c8-6640-4605-abb9-399c568d0048'
#$appSecretConnectorSP = oTRzc6r0irZHwAOvn3s9FaLwoPkvKNYeu3nSUdIIGsg=
$spName = "https://tryane211.sharepoint.com"
$tenantName='tryane211.onmicrosoft.com'

Connect-PnPOnline -ClientId $appIdConnectorSP -Tenant $tenantName -CertificatePath .\keys\tryane.pfx -CertificatePassword (Read-Host -AsSecureString) -Url $spName -AzureEnvironment Production
Get-PnPSite