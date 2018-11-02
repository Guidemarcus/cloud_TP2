# Guide pour faire une simulation
Si l'on veut faire la simulation suivante dans le local L4712: <br/>
- 1 Client <br/>
- 1 Service de nom <br/>
- 1 Repartiteur de charge <br/>
- n Serveur de calcul <br/>

# Service de nom
Dans un terminal, ex�cutez les commandes suivantes
```
ssh L4712-16 // Se connecter a une machine du local L4712
cd pathVersDossierDuProjet
cd bin // Se d�placer vers le dossier bin du projet
rmiregistry 5001 // Partir rmiregistry sur le port 5001
```
Dans un autre terminal, ex�cutez les commandes suivantes
```
// Se connecter a une machine du local L4712
// Note: La machine doit etre la meme que celle du rmiregistry
ssh L4712-16
cd pathVersDossierDuProjet
./serviceDeNom.sh
```

# Repartiteur de charge
Dans un terminal, ex�cutez les commandes suivantes
```
ssh L4712-17 // Se connecter a une machine du local L4712
cd pathVersDossierDuProjet
cd bin // Se d�placer vers le dossier bin du projet
rmiregistry 5001 // Partir rmiregistry sur le port 5001
```
Dans un autre terminal, ex�cutez les commandes suivantes
```
// Se connecter a une machine du local L4712
// Note: La machine doit etre la meme que celle du rmiregistry
ssh L4712-17
cd pathVersDossierDuProjet
./loadBalancer.sh
```

# Serveur de calcul
On peut ex�cuter ces commandes sur autant de machines diff�rentes selon le nombre de serveurs de calcul qu'on veut.
Dans un terminal, ex�cutez les commandes suivantes
```
ssh L4712-18 // Se connecter a une machine du local L4712
cd pathVersDossierDuProjet
cd bin // Se d�placer vers le dossier bin du projet
rmiregistry 5001 // Partir rmiregistry sur le port 5001
```
Dans un autre terminal, ex�cutez les commandes suivantes
```
// Se connecter a une machine du local L4712
// Note: La machine doit etre la meme que celle du rmiregistry
ssh L4712-18
cd pathVersDossierDuProjet
./serverCalcul.sh 5 0 // 1er argument: Capacite (C), 2e argument: Maliciousness (Nombre entre 0 et 100).
```

# Client
Dans un terminal, ex�cutez les commandes suivantes
```
cd pathVersDossierDuProjet
./client.sh ./operations-588 0 // 1er argument: Chemin vers le fichier d'op�rations, 2e argument: Mode 1=Securise 0=Non-securise
```
