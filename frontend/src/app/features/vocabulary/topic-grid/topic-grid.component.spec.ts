import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TopicGridComponent } from './topic-grid.component';
import { TopicService } from '../../../core/services/topic.service';
import { TopicPage } from '../../../core/models/topic.model';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { PageEvent } from '@angular/material/paginator';

const makePage = (status: 'ACTIVE' | 'DONE', count: number, total = count): TopicPage => ({
  content: Array.from({ length: count }, (_, i) => ({
    id: `id-${i}`,
    name: `Topic ${i}`,
    description: '',
    externalApiRef: '',
    vocabularyCount: i * 2,
    status,
    createdAt: new Date().toISOString(),
  })),
  totalElements: total,
  totalPages: Math.ceil(total / 18),
  number: 0,
  size: 18,
  first: true,
  last: total <= 18,
});

describe('TopicGridComponent', () => {
  let fixture: ComponentFixture<TopicGridComponent>;
  let component: TopicGridComponent;
  let topicServiceSpy: jasmine.SpyObj<TopicService>;

  beforeEach(async () => {
    topicServiceSpy = jasmine.createSpyObj('TopicService', ['getPaged']);
    topicServiceSpy.getPaged.and.returnValue(of(makePage('ACTIVE', 5)));

    await TestBed.configureTestingModule({
      imports: [TopicGridComponent, NoopAnimationsModule],
      providers: [{ provide: TopicService, useValue: topicServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Initial load ───────────────────────────────────────────────────────────

  it('should load ACTIVE topics on init', () => {
    expect(topicServiceSpy.getPaged).toHaveBeenCalledOnceWith('ACTIVE', 0, 18);
    expect(component.topics().length).toBe(5);
    expect(component.activeTab()).toBe('ACTIVE');
  });

  it('should request exactly 18 items per page', () => {
    const [, , size] = topicServiceSpy.getPaged.calls.mostRecent().args;
    expect(size).toBe(18);
  });

  // ── Tab switching ─────────────────────────────────────────────────────────

  it('should switch to DONE tab and reset page to 0', fakeAsync(() => {
    topicServiceSpy.getPaged.and.returnValue(of(makePage('DONE', 3)));
    component.currentPage.set(2); // simulate being on page 2

    component.onTabChange(1); // index 1 = DONE
    tick();

    expect(component.activeTab()).toBe('DONE');
    expect(component.currentPage()).toBe(0);
    expect(topicServiceSpy.getPaged).toHaveBeenCalledWith('DONE', 0, 18);
  }));

  it('should switch back to ACTIVE tab', fakeAsync(() => {
    topicServiceSpy.getPaged.and.returnValue(of(makePage('ACTIVE', 5)));
    component.onTabChange(0);
    tick();

    expect(component.activeTab()).toBe('ACTIVE');
    expect(topicServiceSpy.getPaged).toHaveBeenCalledWith('ACTIVE', 0, 18);
  }));

  // ── Pagination ────────────────────────────────────────────────────────────

  it('should request page 2 when paginator emits page event', fakeAsync(() => {
    topicServiceSpy.getPaged.and.returnValue(of(makePage('ACTIVE', 18, 54)));
    const event: PageEvent = { pageIndex: 2, pageSize: 18, length: 54 };

    component.onPageChange(event);
    tick();

    expect(component.currentPage()).toBe(2);
    expect(topicServiceSpy.getPaged).toHaveBeenCalledWith('ACTIVE', 2, 18);
  }));

  it('should expose totalElements from page response', () => {
    topicServiceSpy.getPaged.and.returnValue(of(makePage('ACTIVE', 18, 72)));
    component.onTabChange(0);

    expect(component.totalElements()).toBe(72);
  });

  // ── Loading state ─────────────────────────────────────────────────────────

  it('should set loading=false after topics arrive', () => {
    expect(component.loading()).toBeFalse();
  });

  // ── trackById ─────────────────────────────────────────────────────────────

  it('trackById should return topic.id', () => {
    const topic = component.topics()[0];
    expect(component.trackById(0, topic)).toBe(topic.id);
  });
});
