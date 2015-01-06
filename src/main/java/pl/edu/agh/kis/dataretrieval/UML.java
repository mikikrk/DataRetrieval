package pl.edu.agh.kis.dataretrieval;

/**
 * PlantUML Diagrams 
 * 
 * Use Cases
 * @startuml

:Urzutkownik: as user
:Baza danych: as db
:Strona internetowa: as site
left to right direction
rectangle "System do pozyskiwania informacji ze stron WWW" {
	(U¿ycie trybu okienkowego) as windowMode
	(U¿ycie trybu konsolowego) as consoleMode
	user -right-> windowMode
	windowMode ..> (Dodanie nowej konfiguracji) : <<include>>
	windowMode ..> (Utworzenie nowej konfiguracji) : <<include>>
	windowMode ..> (Edycja wybranej konfiguracji) : <<include>>
	windowMode ..> (Wyœwietlenie wybranej konfiguracji) : <<include>>
	windowMode ..> (Sprawdzenie sk³adni wybranej konfiguracji) : <<include>>
	windowMode ..> (Usuniêcie wybranej konfiguracji) : <<include>>
	
	(Wydobywanie informacji) as processing
	(Wydobywanie w trybie standardowym) as standard
	(Wydobywanie w trybie masowym) as bulk
	windowMode .> processing : <<include>>
	processing <.. standard: <<extend>>
	processing <.. bulk : <<extend>>

	db -- processing 
	
	
	user --> consoleMode
	consoleMode ..> bulk : <<include>>
	
	usecase standardForm as "Pobranie danych na podstawie
	jednego ustawienia pól w formularzach"
	standardForm .> standard : <<extend>>
	
	usecase bulkAntyBot as "Pobranie danych partiami
	w odstêpach czasowych"
	usecase bulkMultiValues as "Pobranie danych na podstawie
	ró¿nych ustawieñ pól w formularzach"
	bulkAntyBot .> bulk : <<extend>>
	bulk <. bulkMultiValues : <<extend>>
}

@enduml

 * Sequence
@startuml
actor U¿ytkownik as user
participant "Interfejs\ngraficzny" as gui
participant "Przetwarzanie\nkonfiguracji" as config
participant "Przeszukiwanie\nstron" as crawl
participant "Wydobywanie\ninformacji" as retr
participant "Interfejs\nbazodanowy" as db

alt tryb okienkowy
	user -> gui : uruchomienie\nprogramu
	gui -> gui : wybór konfiguracji
	gui -> config : przekazanie\nkonfiguracji\ndo przetwarzania
	gui -> gui :otworzenie okienka\nz postêpem procesu
else tryb konsolowy
	user -> config : uruchomienie programu i przekazanie\n konfiguracji do przetwarzania
end
config -> config : sprawdzenie sk³adni
config -> config : wczytanie obydwu\nkonfiguracji
config -> crawl : przekazanie\nkonfiguracji\ndo wyszukiwania
activate crawl
loop Iloœæ alternatywnych ustawieñ formularzy w trybie masowym, albo maksymalna iloœæ rekordów\nW trybie standardowym jedno przejœcie
	alt tryb masowy
		crawl -> crawl : ustawienie wartosci\nformularzy ustawianych\nw danej iteracji
	end
	group dla ka¿dego elementu w konfiguracji wyszukiwania
		alt link
			crawl -> crawl : przejœcie do linku
		else formularz
			alt tryb masowy
				crawl -> crawl :automatyczne uzupe³nienie
			else tryb standardowy
				crawl -> gui :przeslanie formularza do uzupelnienia
				activate gui
				gui -> user :wyswietlenie formularza
				user --> gui :uzupe³nienie formularza
				gui --> crawl :przeslanie uzupelnionego formularza
				deactivate gui
			end
		else strona z wynikami
			config -> retr : przekazanie\nkonfiguracji\ndo wydobywania
			loop do pobrania wszystkich stron,\nalbo do maksymalnej iloœci stron
				crawl -> retr :przekazanie kolejnej strony
				activate retr
				retr -> retr :wydobycie\ndanych\nze strony
				retr -> db :przeslanie\nrekordu\ndo zapisania
				db -> db :zapisanie\nrekordu\nw bazie
				db --> retr
				retr --> crawl
				deactivate retr
			end
		else alternatywa zale¿na od uzupe³nionych wartoœci formularza
			crawl -> crawl :wybór odpowiedniej\ngrupy kolejnych elementów
		end
		
	end
end
alt tryb masowy
	crawl -> config :zpisanie stanu\npobierania
end
crawl --> user :zwrócenie komunikatu o zakoñczeniu wydobywania
deactivate crawl
@enduml

 * @author Mikolaj
 *
 */
public class UML {

}
