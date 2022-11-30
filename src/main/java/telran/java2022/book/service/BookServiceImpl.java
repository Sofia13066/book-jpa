package telran.java2022.book.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import telran.java2022.book.dao.AuthorRepository;
import telran.java2022.book.dao.BookRepository;
import telran.java2022.book.dao.PublisherRepository;
import telran.java2022.book.dto.AuthorDto;
import telran.java2022.book.dto.BookDto;
import telran.java2022.book.dto.exceptions.EntityNotFoundException;
import telran.java2022.book.model.Author;
import telran.java2022.book.model.Book;
import telran.java2022.book.model.Publisher;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
	final BookRepository bookRepository;
	final AuthorRepository authorRepository;
	final PublisherRepository publisherRepository;
	final ModelMapper modelMapper;

	@Override
	@Transactional
	public boolean addBook(BookDto bookDto) {
		if (bookRepository.existsById(bookDto.getIsbn())) {
			return false;
		}
		//Publisher
		Publisher publisher = publisherRepository.findById(bookDto.getPublisher())
				.orElse(publisherRepository.save(new Publisher(bookDto.getPublisher())));
		//Author
		Set<Author> authors = bookDto.getAuthors().stream()
									.map(a -> authorRepository.findById(a.getName())
											.orElse(authorRepository.save(new Author(a.getName(), a.getBirthDate()))))
									.collect(Collectors.toSet());
		Book book = new Book(bookDto.getIsbn(), bookDto.getTitle(), authors, publisher);
		bookRepository.save(book);
		return true;
	}

	@Override
	@Transactional
	public BookDto findBookByIsbn(String isbn) {
		Book bookFind = bookRepository.findById(isbn).orElseThrow(EntityNotFoundException::new);
		return modelMapper.map(bookFind, BookDto.class);
	}

	@Override
	@Transactional
	public BookDto removeBook(String isbn) {
		Book bookFind = bookRepository.findById(isbn).orElseThrow(EntityNotFoundException::new);
		BookDto bookDtoToRemove = modelMapper.map(bookFind, BookDto.class);
		bookRepository.deleteById(isbn);
		return bookDtoToRemove;
	}

	@Override
	@Transactional
	public BookDto updateBook(String isbn, String title) {
		Book bookFind = bookRepository.findById(isbn).orElseThrow(EntityNotFoundException::new);
		bookFind.setTitle(title);
		bookRepository.save(bookFind);
		BookDto bookDtoToUpdate = modelMapper.map(bookFind, BookDto.class);
		return bookDtoToUpdate;
	}

	@Override
	@Transactional
	public Iterable<BookDto> findBooksByAuthor(String authorName) {
		Author author = authorRepository.findById(authorName).orElseThrow(EntityNotFoundException::new);
		return bookRepository.findAllBooksByAuthors(author).map(a -> modelMapper.map(a, BookDto.class))
																	.collect(Collectors.toList());

	}

	@Override
	@Transactional
	public Iterable<BookDto> findBooksByPublisher(String publisherName) {
		Publisher publisher = publisherRepository.findById(publisherName).orElseThrow(EntityNotFoundException::new);
		return bookRepository.findAllBooksByPublisher(publisher).map(a -> modelMapper.map(a, BookDto.class))
																			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public Iterable<AuthorDto> findBookAuthors(String isbn) {
		Book bookFind = bookRepository.findById(isbn).orElseThrow(EntityNotFoundException::new);

		return bookFind.getAuthors().stream()
											.map(b -> modelMapper.map(b, AuthorDto.class))
											.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public Iterable<String> findPublishersByAuthor(String authorName) {
		Author author = authorRepository.findById(authorName).orElseThrow(EntityNotFoundException::new);
		return bookRepository.findAllBooksByAuthors(author).map(a -> a.getPublisher().getPublisherName())
																	.collect(Collectors.toList());
	
	}

	@Override
	@Transactional
	public AuthorDto removeAuthor(String authorName) {
		Author author = authorRepository.findById(authorName).orElseThrow(EntityNotFoundException::new);
		bookRepository.findAllBooksByAuthors(author).forEach(a -> {a.getAuthors().remove(authorName);
																bookRepository.save(a);
															});
		authorRepository.delete(author);														
		return modelMapper.map(author, AuthorDto.class);
	}

}
