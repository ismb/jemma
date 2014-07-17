-- Struttura della tabella `commenti`
-- 

CREATE TABLE `commenti` (
  `id_commento` int(6) NOT NULL auto_increment,
  `id_post` varchar(5) NOT NULL default '',
  `autore_commento` varchar(30) NOT NULL default '',
  `testo_commento` text NOT NULL,
  `data_commento` date NOT NULL default '0000-00-00',
  `approvato` enum('0','1') NOT NULL default '0',
  PRIMARY KEY  (`id_commento`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- 
-- Dump dei dati per la tabella `commenti`
-- 


-- --------------------------------------------------------

-- 
-- Struttura della tabella `login`
-- 

CREATE TABLE `login` (
  `id_login` int(1) NOT NULL auto_increment,
  `username_login` varchar(10) NOT NULL default '',
  `password_login` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`id_login`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=21 ;

-- 
-- Dump dei dati per la tabella `login`
-- 

INSERT INTO `login` (`id_login`, `username_login`, `password_login`) VALUES (1, 'admin', '5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8');

-- --------------------------------------------------------

-- 
-- Struttura della tabella `post`
-- 

CREATE TABLE `post` (
  `id_post` int(5) NOT NULL auto_increment,
  `titolo_post` varchar(255) NOT NULL default '',
  `testo_post` text NOT NULL,
  `autore_post` varchar(30) NOT NULL default '',
  `data_post` date NOT NULL default '0000-00-00',
  PRIMARY KEY  (`id_post`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- 
-- Dump dei dati per la tabella `post`
-- 

